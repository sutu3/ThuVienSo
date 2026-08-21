package org.example.thuvienso.Helper;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.thuvienso.Module.DocumentEntity;
import org.example.thuvienso.Repo.DocumentRepo;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

@Component
public class ThumbnailGenerator {

    private static final String BUCKET = "thuvienso";
    private static final int WIDTH = 300;
    private static final int HEIGHT = 400;
    private static final float PDF_DPI = 100f;
    private static final String REAL_PREFIX = "thumbnails/";

    private final LocalStorage localStorage;
    private final DocumentRepo documentRepo;

    public ThumbnailGenerator(LocalStorage localStorage,
                              DocumentRepo documentRepo) {
        this.localStorage = localStorage;
        this.documentRepo = documentRepo;
    }

    /**
     * Render trang đầu tiên (index 0) của PDF thành BufferedImage.
     */
    public String generate(MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null) return "icons/file.png";

            // 1) Ảnh (png/jpg) -> render trực tiếp
            if (contentType.startsWith("image/")) {
                BufferedImage source = ImageIO.read(file.getInputStream());
                if (source == null) return "icons/file.png";
                return saveThumbnail(source);
            }

            // 2) PDF -> render TRANG ĐẦU thành ảnh rồi tạo thumbnail
            if (contentType.equals("application/pdf")) {
                BufferedImage source = renderFirstPage(file.getBytes());
                if (source == null) return "icons/pdf.png"; // fallback nếu render lỗi
                return saveThumbnail(source);
            }

            // 3) Các loại còn lại (mp3/audio/video...) -> icon theo loại
            return iconFor(contentType, file.getOriginalFilename());
        } catch (Exception e) {
            return "icons/file.png"; // không chặn upload
        }
    }

    /**
     * Resize ảnh về kích thước thumbnail, upload lên MinIO, trả object name.
     */
    private BufferedImage renderFirstPage(byte[] pdfBytes) {
        try (PDDocument pdf = org.apache.pdfbox.Loader.loadPDF(pdfBytes)) {
            if (pdf.getNumberOfPages() == 0) return null;
            return new PDFRenderer(pdf).renderImageWithDPI(0, PDF_DPI);
        } catch (Exception e) {
            return null;
        }
    }

    /** Resize ảnh về kích thước thumbnail, ghi ra ổ đĩa cục bộ, trả object name. */
    private String saveThumbnail(BufferedImage source) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(source).size(WIDTH, HEIGHT).outputFormat("jpg").toOutputStream(out);
        byte[] bytes = out.toByteArray();

        String objectName = REAL_PREFIX + UUID.randomUUID() + ".jpg";
        return localStorage.store(bytes, objectName);
    }

    private String iconFor(String contentType, String fileName) {
        String ct = contentType == null ? "" : contentType.toLowerCase();
        String name = fileName == null ? "" : fileName.toLowerCase();

        if (ct.equals("application/pdf")) return "icons/pdf.png";
        if (ct.contains("word") || name.endsWith(".doc") || name.endsWith(".docx")) return "icons/word.png";
        if (ct.contains("excel") || ct.contains("spreadsheet") || name.endsWith(".xls") || name.endsWith(".xlsx"))
            return "icons/excel.png";
        if (ct.contains("powerpoint") || ct.contains("presentation") || name.endsWith(".ppt") || name.endsWith(".pptx"))
            return "icons/powerpoint.png";
        if (ct.startsWith("audio/")) return "icons/audio.png";
        if (ct.startsWith("video/")) return "icons/video.png";
        if (ct.contains("zip") || ct.contains("rar") || ct.contains("7z") || ct.contains("compressed"))
            return "icons/archive.png";
        if (ct.startsWith("text/")) return "icons/txt.png";
        if (name.matches(".*\\.(java|js|ts|py|c|cpp|cs|html|css|json|xml)$")) return "icons/code.png";

        return "icons/file.png";
    }

    /**
     * Sinh thumbnail cho file, gán cho document nếu hợp lý, và trả object name.
     */
    public String applyThumbnail(MultipartFile file, DocumentEntity document) {
        String thumbObject = generate(file);
        if (shouldUpdateDocumentThumbnail(document, file, thumbObject)) {
            document.setThumbnail(thumbObject);
            documentRepo.save(document);
        }
        return thumbObject;
    }

    private boolean shouldUpdateDocumentThumbnail(DocumentEntity document,
                                                  MultipartFile file,
                                                  String thumbObject) {
        if (thumbObject == null) return false;

        String contentType = file.getContentType();
        String current = document.getThumbnail();
        boolean docHasNothing = current == null || current.isBlank();
        boolean docHasRealThumb = current != null && current.startsWith(REAL_PREFIX);

        // 1) Ảnh -> ảnh bìa thật, luôn ưu tiên ghi đè
        if (contentType != null && contentType.startsWith("image/")) {
            return true;
        }

        // 2) PDF render thật -> ghi đè khi document CHƯA có ảnh thật
        boolean isRealThumb = thumbObject.startsWith(REAL_PREFIX);
        if (isRealThumb) {
            return !docHasRealThumb;
        }

        // 3) Còn lại (mp3/audio/video -> icon): chỉ set khi document chưa có gì
        return docHasNothing;
    }
}