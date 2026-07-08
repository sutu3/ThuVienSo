package org.example.thuvienso.Helper;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Component
public class BuildPath {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public String buildObjectPath(MultipartFile file) {

        String contentType = file.getContentType();
        String original = file.getOriginalFilename();

        // 1. Tách extension (đối chiếu để phân loại chính xác hơn contentType)
        String extension = extractExtension(original);

        // 2. Sanitize tên file gốc: bỏ đường dẫn, thay ký tự đặc biệt bằng "_"
        String safeName = sanitize(original);

        // 3. Tránh trùng tên bằng UUID
        String fileName = UUID.randomUUID() + "_" + safeName;

        // 4. Ngày để tránh dồn quá nhiều object vào một "thư mục"
        String datePath = LocalDate.now().format(DATE_FORMAT);

        // 5. Xác định thư mục theo contentType + extension
        String folder = resolveFolder(contentType, extension);

        return folder + "/" + datePath + "/" + fileName;
    }

    private String resolveFolder(String contentType, String extension) {
        String ct = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);

        // Ảnh
        if (ct.startsWith("image/") || isImageExt(extension)) {
            return "images";
        }
        // Video
        if (ct.startsWith("video/") || isVideoExt(extension)) {
            return "videos";
        }
        // Audio
        if (ct.startsWith("audio/") || isAudioExt(extension)) {
            return "audio";
        }
        // PDF
        if (ct.equals("application/pdf") || extension.equals("pdf")) {
            return "documents/pdf";
        }
        // Word
        if (ct.contains("word")
                || ct.contains("wordprocessingml")
                || extension.equals("doc") || extension.equals("docx")) {
            return "documents/docx";
        }
        // Excel
        if (ct.contains("excel")
                || ct.contains("spreadsheetml")
                || extension.equals("xls") || extension.equals("xlsx")) {
            return "documents/excel";
        }
        // PowerPoint
        if (ct.contains("powerpoint")
                || ct.contains("presentationml")
                || extension.equals("ppt") || extension.equals("pptx")) {
            return "documents/ppt";
        }
        // Text
        if (ct.startsWith("text/") || extension.equals("txt")) {
            return "documents/text";
        }
        // File nén
        if (ct.contains("zip") || ct.contains("compressed")
                || extension.equals("zip") || extension.equals("rar")
                || extension.equals("7z")) {
            return "archives";
        }

        return "others";
    }

    private boolean isImageExt(String ext) {
        return ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg")
                || ext.equals("gif") || ext.equals("webp") || ext.equals("bmp");
    }

    private boolean isVideoExt(String ext) {
        return ext.equals("mp4") || ext.equals("avi") || ext.equals("mkv")
                || ext.equals("mov") || ext.equals("wmv");
    }

    private boolean isAudioExt(String ext) {
        return ext.equals("mp3") || ext.equals("wav") || ext.equals("flac")
                || ext.equals("aac") || ext.equals("ogg");
    }

    private String extractExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) return "";
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String sanitize(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "unknown";
        }
        // Bỏ mọi phần đường dẫn (chống path traversal như ../ hoặc \)
        String name = fileName.replace("\\", "/");
        name = name.substring(name.lastIndexOf('/') + 1);
        // Thay ký tự không an toàn bằng "_"
        name = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        return name.isBlank() ? "unknown" : name;
    }
}