package org.example.thuvienso.Helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Response.FileUploadResponse;
import org.example.thuvienso.Service.MinioService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Upload ảnh base64 trong HTML và thay src bằng URL file nội bộ. */
@Component
@RequiredArgsConstructor
@Slf4j
public class NewsContentImageProcessor {
    private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024;
    private static final Pattern EMBEDDED_IMAGE = Pattern.compile(
            "(?is)(<img\\b[^>]*?\\bsrc\\s*=\\s*(['\"]))(data:image/([a-zA-Z0-9.+-]+);base64,([^'\"]+))(\\2)"
    );

    private final MinioService minioService;

    public String uploadEmbeddedImages(String html) {
        if (!StringUtils.hasText(html)) return html;

        Matcher matcher = EMBEDDED_IMAGE.matcher(html);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String replacement = matcher.group(0);
            try {
                String mimeSubtype = matcher.group(4).toLowerCase();
                byte[] content = Base64.getDecoder().decode(matcher.group(5).replaceAll("\\s", ""));
                if (content.length > MAX_IMAGE_BYTES) throw new IllegalArgumentException("Ảnh nhúng vượt quá 10 MB");

                FileUploadResponse uploaded = minioService.upload(
                        content,
                        "news-content." + extensionFor(mimeSubtype),
                        "image/" + mimeSubtype
                );
                replacement = matcher.group(1) + uploaded.getPreviewUrl() + matcher.group(6);
            } catch (Exception ex) {
                log.warn("Không thể upload ảnh nhúng trong nội dung bài viết", ex);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String extensionFor(String subtype) {
        return switch (subtype) {
            case "jpeg" -> "jpg";
            case "svg+xml" -> "svg";
            default -> subtype.replaceAll("[^a-z0-9]", "");
        };
    }
}
