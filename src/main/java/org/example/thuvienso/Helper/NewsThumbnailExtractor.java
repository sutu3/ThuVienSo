package org.example.thuvienso.Helper;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.example.thuvienso.Dto.Response.FileUploadResponse;
import org.example.thuvienso.Service.MinioService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Lấy ảnh đầu tiên trong HTML bài viết để dùng làm ảnh đại diện. */
@Component
@Slf4j
public class NewsThumbnailExtractor {
    private static final int MAX_THUMBNAIL_LENGTH = 256;
    private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024;
    private static final String NEWS_THUMBNAIL_FILE_NAME = "news-thumbnail.jpg";
    private static final Pattern IMAGE_SOURCE = Pattern.compile("(?is)<img\\b[^>]*?\\bsrc\\s*=\\s*(['\"])(.*?)\\1");
    private final LocalStorage localStorage;
    private final MinioService minioService;

    public NewsThumbnailExtractor(LocalStorage localStorage, MinioService minioService) {
        this.localStorage = localStorage;
        this.minioService = minioService;
    }

    /** Tạo thumbnail JPG nội bộ từ ảnh đầu tiên trong nội dung HTML. */
    public String generate(String html) {
        String source = extractSource(html);
        if (source == null) return null;
        try (InputStream input = openSource(source)) {
            byte[] imageBytes = input.readNBytes(MAX_IMAGE_BYTES + 1);
            if (imageBytes.length > MAX_IMAGE_BYTES) return null;
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) return null;

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Thumbnails.of(image).size(600, 360).outputFormat("jpg").outputQuality(0.85).toOutputStream(output);
            FileUploadResponse uploaded = minioService.upload(output.toByteArray(), NEWS_THUMBNAIL_FILE_NAME, "image/jpeg");
            return uploaded.getObjectName();
        } catch (Exception ignored) {
            log.error("Lỗi generate thumbnail bài viết", ignored);
            return null;
        }
    }

    public void delete(String objectName) throws IOException {
        if (objectName != null && objectName.endsWith("_" + NEWS_THUMBNAIL_FILE_NAME)) localStorage.delete(objectName);
    }

    private String extractSource(String html) {
        if (!StringUtils.hasText(html)) return null;
        Matcher matcher = IMAGE_SOURCE.matcher(html);
        if (!matcher.find()) return null;

        String source = matcher.group(2).trim();
        if (source.length() > MAX_THUMBNAIL_LENGTH || !isAllowedSource(source)) return null;
        return source;
    }

    private InputStream openSource(String source) throws IOException {
        int rawPathIndex = source.indexOf("/files/raw/");
        if (rawPathIndex >= 0) return localStorage.load(source.substring(rawPathIndex + "/files/raw/".length()));
        HttpURLConnection connection = (HttpURLConnection) URI.create(source).toURL().openConnection();
        connection.setConnectTimeout(5_000);
        connection.setReadTimeout(10_000);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("User-Agent", "ThuVienSo-NewsThumbnail/1.0");
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) throw new IOException("Cannot download image");
        return connection.getInputStream();
    }

    private boolean isAllowedSource(String source) {
        return source.startsWith("https://") || source.startsWith("http://") || source.contains("/files/raw/");
    }
}
