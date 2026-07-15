package org.example.thuvienso.Helper;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

@Component
public class ThumbnailGenerator {

    private static final String BUCKET = "thuvienso";
    private static final int WIDTH = 300;
    private static final int HEIGHT = 400;

    private static final Set<String> OFFICE_TYPES = Set.of(

            "application/msword",

            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",

            "application/vnd.ms-excel",

            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",

            "application/vnd.ms-powerpoint",

            "application/vnd.openxmlformats-officedocument.presentationml.presentation",

            "application/vnd.oasis.opendocument.text",

            "application/vnd.oasis.opendocument.spreadsheet",

            "application/vnd.oasis.opendocument.presentation"
    );
    private static final Set<String> VIDEO_TYPES = Set.of(

            "video/mp4",

            "video/x-msvideo",

            "video/x-matroska",

            "video/quicktime",

            "video/webm",

            "video/x-ms-wmv"
    );


    // các content-type Office cần convert sang PDF trước

    private final MinioClient minioClient;
    private final DocumentConverter documentConverter;

    public ThumbnailGenerator(MinioClient minioClient, DocumentConverter documentConverter) {
        this.minioClient = minioClient;
        this.documentConverter = documentConverter;
    }

    /** Trả về objectName của thumbnail, hoặc null nếu không tạo được */
    public String generate(MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null) return null;

            BufferedImage source = null;

            if (contentType.startsWith("image/")) {
                source = ImageIO.read(file.getInputStream());
            } else if (contentType.equals("application/pdf")) {
                source = renderFirstPage(file.getBytes());
            } else if (OFFICE_TYPES.contains(contentType)) {
                byte[] pdfBytes = convertOfficeToPdf(file.getBytes());
                source = renderFirstPage(pdfBytes);
            }
            // Video vẫn cần ffmpeg (chưa hỗ trợ)

            if (source == null) return null;

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
        } catch (Exception e) {
            return null; // không chặn upload nếu tạo thumbnail lỗi
        }
    }

    /** Render trang đầu của PDF (byte) thành ảnh — tái dùng cho cả PDF và Office đã convert */
    private BufferedImage renderFirstPage(byte[] pdfBytes) throws Exception {
        try (PDDocument pdf = org.apache.pdfbox.Loader.loadPDF(pdfBytes)) {
            return new PDFRenderer(pdf).renderImageWithDPI(0, 100); // trang đầu
        }
    }

    /** Convert file Office (byte) sang PDF (byte) bằng LibreOffice qua JODConverter */
    private byte[] convertOfficeToPdf(byte[] officeBytes) throws Exception {
        try (InputStream in = new ByteArrayInputStream(officeBytes);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            documentConverter
                    .convert(in)
                    .to(out)
                    .as(DefaultDocumentFormatRegistry.PDF)
                    .execute();
            return out.toByteArray();
        }
    }
}