// src/main/java/org/example/thuvienso/Helper/ThumbnailGenerator.java  
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

    private final MinioClient minioClient;

    public ThumbnailGenerator(MinioClient minioClient) {
        this.minioClient = minioClient;
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
                try (PDDocument pdf = org.apache.pdfbox.Loader.loadPDF(file.getBytes())) {
                    source = new PDFRenderer(pdf).renderImageWithDPI(0, 100); // trang đầu  
                }
            }
            // Video cần ffmpeg (xem ghi chú bên dưới)  

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
}