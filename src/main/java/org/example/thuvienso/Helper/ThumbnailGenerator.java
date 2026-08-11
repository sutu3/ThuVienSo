package org.example.thuvienso.Helper;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
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

    private final MinioClient minioClient;

    public ThumbnailGenerator(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * Trả về objectName của thumbnail (ảnh render thật hoặc icon theo loại file)
     */
    public String generate(MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null) return "icons/file.png";

            // 1) Ảnh -> render trực tiếp
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

            // 3) Các loại còn lại -> icon theo loại
            return iconFor(contentType, file.getOriginalFilename());
        } catch (Exception e) {
            return "icons/file.png"; // không chặn upload
        }
    }

    /**
     * Render trang đầu tiên (index 0) của PDF thành BufferedImage.
     */
    private BufferedImage renderFirstPage(byte[] pdfBytes) {
        try (PDDocument pdf = org.apache.pdfbox.Loader.loadPDF(pdfBytes)) {
            if (pdf.getNumberOfPages() == 0) return null;
            return new PDFRenderer(pdf).renderImageWithDPI(0, PDF_DPI);
        } catch (Exception e) {
            return null; // để generate() fallback về icon
        }
    }

    /**
     * Resize ảnh về kích thước thumbnail, upload lên MinIO, trả object name.
     */
    private String saveThumbnail(BufferedImage source) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(source).size(WIDTH, HEIGHT).outputFormat("jpg").toOutputStream(out);
        byte[] bytes = out.toByteArray();

        String objectName = "thumbnails/" + UUID.randomUUID() + ".jpg";
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET)
                    .object(objectName)
                    .stream(in, bytes.length, -1)
                    .contentType("image/jpeg")
                    .build());
        }
        return objectName;
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
}