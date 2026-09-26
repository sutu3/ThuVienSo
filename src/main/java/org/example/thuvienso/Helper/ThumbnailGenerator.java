package org.example.thuvienso.Helper;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.thuvienso.Module.DocumentEntity;
import org.example.thuvienso.Repo.DocumentRepo;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import java.io.File;
import java.nio.file.Files;

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
                BufferedImage card =
                        createDocumentCard(
                                source,
                                file.getOriginalFilename(),
                                "PDF"
                        );
                if (source == null) return "icons/pdf.png"; // fallback nếu render lỗi
                return saveThumbnail(card);
            }

            // 3) VIDEO -> trích 1 khung hình làm thumbnail thật
            if (contentType.startsWith("video/")) {
                BufferedImage source = grabVideoFrame(file);
                if (source == null) return "icons/video.png"; // fallback nếu grab lỗi
                return saveThumbnail(source);
            }
            // 3) PowerPoint -> render slide đầu THẬT
            if (isPowerPoint(contentType, file.getOriginalFilename())) {
                BufferedImage source = renderFirstSlide(file);
                if (source == null) return "icons/powerpoint.png";
                return saveThumbnail(source);
            }

            // 4) Word / Excel -> POI không render layout, vẽ ảnh text đơn giản
            if (isWord(contentType, file.getOriginalFilename())) {
                BufferedImage source = renderTextPreview(extractWordText(file));
                if (source == null) return "icons/word.png";
                return saveThumbnail(source);
            }
            if (isExcel(contentType, file.getOriginalFilename())) {
                BufferedImage source = renderTextPreview(extractExcelText(file));
                if (source == null) return "icons/excel.png";
                return saveThumbnail(source);
            }

            // 5) Các loại còn lại (mp3/audio/video...) -> icon theo loại
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
    /** Trích 1 khung hình từ video (bytedeco/JavaCV) làm ảnh nguồn cho thumbnail. */
    private BufferedImage grabVideoFrame(MultipartFile file) {
        File temp = null;
        try {
            // FFmpegFrameGrabber cần đường dẫn file thật -> ghi ra file tạm
            temp = File.createTempFile("video-thumb-", ".tmp");
            file.transferTo(temp);

            try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(temp)) {
                grabber.start();

                // nhảy tới ~ giây thứ 1 (hoặc giữa video) để tránh frame đen đầu clip
                long durationUs = grabber.getLengthInTime();

                long seekUs = Math.max(
                        1_000_000L,
                        (long) (durationUs * 0.10)
                );

                grabber.setTimestamp(
                        Math.min(seekUs, durationUs)
                );
                //if (seekUs > 0) grabber.setTimestamp(seekUs);

                try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
                    // lấy frame ảnh đầu tiên gặp được
                    Frame frame;
                    int guard = 0;
                    while ((frame = grabber.grabImage()) != null && guard++ < 50) {
                        BufferedImage img = converter.getBufferedImage(frame);
                        if (img != null) {
                            grabber.stop();
                            return img;
                        }
                    }
                }
                grabber.stop();
            }
            return null;
        } catch (Exception e) {
            return null; // không chặn upload
        } finally {
            if (temp != null) {
                try { Files.deleteIfExists(temp.toPath()); } catch (Exception ignore) {}
            }
        }
    }
    // ===== PowerPoint: render slide đầu ra ảnh THẬT =====
    private BufferedImage renderFirstSlide(MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        try (InputStream in = file.getInputStream()) {
            if (name.endsWith(".pptx")) {
                try (org.apache.poi.xslf.usermodel.XMLSlideShow ppt =
                             new org.apache.poi.xslf.usermodel.XMLSlideShow(in)) {
                    if (ppt.getSlides().isEmpty()) return null;
                    java.awt.Dimension size = ppt.getPageSize();
                    BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics2D g = img.createGraphics();
                    g.setColor(java.awt.Color.WHITE);
                    g.fillRect(0, 0, size.width, size.height);
                    ppt.getSlides().get(0).draw(g);   // vẽ slide đầu
                    g.dispose();
                    return img;
                }
            } else { // .ppt cũ (HSLF, cần poi-scratchpad)
                try (org.apache.poi.hslf.usermodel.HSLFSlideShow ppt =
                             new org.apache.poi.hslf.usermodel.HSLFSlideShow(in)) {
                    if (ppt.getSlides().isEmpty()) return null;
                    java.awt.Dimension size = ppt.getPageSize();
                    BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics2D g = img.createGraphics();
                    g.setColor(java.awt.Color.WHITE);
                    g.fillRect(0, 0, size.width, size.height);
                    ppt.getSlides().get(0).draw(g);
                    g.dispose();
                    return img;
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    // ===== Word: chỉ lấy text (POI không render layout) =====
    private String extractWordText(MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        try (InputStream in = file.getInputStream()) {
            if (name.endsWith(".docx")) {
                try (org.apache.poi.xwpf.usermodel.XWPFDocument doc =
                             new org.apache.poi.xwpf.usermodel.XWPFDocument(in);
                     org.apache.poi.xwpf.extractor.XWPFWordExtractor ex =
                             new org.apache.poi.xwpf.extractor.XWPFWordExtractor(doc)) {
                    return ex.getText();
                }
            } else { // .doc cũ (HWPF, poi-scratchpad)
                try (org.apache.poi.hwpf.HWPFDocument doc = new org.apache.poi.hwpf.HWPFDocument(in);
                     org.apache.poi.hwpf.extractor.WordExtractor ex =
                             new org.apache.poi.hwpf.extractor.WordExtractor(doc)) {
                    return ex.getText();
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    // ===== Excel: lấy vài dòng đầu của sheet đầu =====
    private String extractExcelText(MultipartFile file) {
        try (InputStream in = file.getInputStream();
             org.apache.poi.ss.usermodel.Workbook wb =
                     org.apache.poi.ss.usermodel.WorkbookFactory.create(in)) {
            if (wb.getNumberOfSheets() == 0) return null;
            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
            org.apache.poi.ss.usermodel.DataFormatter fmt = new org.apache.poi.ss.usermodel.DataFormatter();
            StringBuilder sb = new StringBuilder();
            int rowCount = 0;
            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                if (rowCount++ > 20) break;              // giới hạn 20 dòng
                int cellCount = 0;
                for (org.apache.poi.ss.usermodel.Cell cell : row) {
                    if (cellCount++ > 6) break;          // giới hạn 6 cột
                    sb.append(fmt.formatCellValue(cell)).append("\t");
                }
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    // ===== Vẽ text ra ảnh (fallback cho Word/Excel) =====
    private BufferedImage renderTextPreview(String text) {
        if (text == null || text.isBlank()) return null;
        int w = 600, h = 800;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(java.awt.Color.BLACK);
        g.setFont(new java.awt.Font("Serif", java.awt.Font.PLAIN, 16));
        g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int x = 20, y = 30, lineH = 22, maxLines = (h - y) / lineH;
        String[] rawLines = text.split("\\r?\\n");
        int drawn = 0;
        for (String line : rawLines) {
            if (drawn >= maxLines) break;
            // cắt dòng dài cho vừa bề ngang
            while (line.length() > 70) {
                g.drawString(line.substring(0, 70), x, y);
                y += lineH; drawn++;
                line = line.substring(70);
                if (drawn >= maxLines) break;
            }
            if (drawn >= maxLines) break;
            g.drawString(line, x, y);
            y += lineH; drawn++;
        }
        g.dispose();
        return img;
    }

    // ===== helper nhận diện loại =====
    private boolean isPowerPoint(String ct, String name) {
        ct = ct == null ? "" : ct.toLowerCase();
        name = name == null ? "" : name.toLowerCase();
        return ct.contains("powerpoint") || ct.contains("presentation")
                || name.endsWith(".ppt") || name.endsWith(".pptx");
    }
    private boolean isWord(String ct, String name) {
        ct = ct == null ? "" : ct.toLowerCase();
        name = name == null ? "" : name.toLowerCase();
        return ct.contains("word") || name.endsWith(".doc") || name.endsWith(".docx");
    }
    private boolean isExcel(String ct, String name) {
        ct = ct == null ? "" : ct.toLowerCase();
        name = name == null ? "" : name.toLowerCase();
        return ct.contains("excel") || ct.contains("spreadsheet")
                || name.endsWith(".xls") || name.endsWith(".xlsx");
    }
    private BufferedImage createDocumentCard(
            BufferedImage preview,
            String fileName,
            String type
    ) throws IOException {
        int width = 600;
        int height = 800;

        BufferedImage canvas =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D g = canvas.createGraphics();

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        // Background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // Preview
        if (preview != null) {

            int previewWidth = 520;
            int previewHeight = 600;

            BufferedImage resized =
                    Thumbnails.of(preview)
                            .size(previewWidth, previewHeight)
                            .asBufferedImage();

            int x = (width - resized.getWidth()) / 2;
            int y = 40;

            g.drawImage(resized, x, y, null);
        }

        // Type
        g.setColor(new Color(60, 60, 60));

        g.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        24
                )
        );

        g.drawString(
                type,
                30,
                700
        );

        // File name
        g.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        18
                )
        );

        String displayName =
                shortenFileName(fileName, 45);

        g.drawString(
                displayName,
                30,
                740
        );

        g.dispose();

        return canvas;
    }
    private String shortenFileName(String fileName, int maxLength) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }

        if (fileName.length() <= maxLength) {
            return fileName;
        }

        int extensionIndex = fileName.lastIndexOf(".");

        // Không có extension
        if (extensionIndex <= 0) {
            return fileName.substring(0, maxLength - 3) + "...";
        }

        String extension = fileName.substring(extensionIndex);
        String name = fileName.substring(0, extensionIndex);

        int availableLength = maxLength - extension.length() - 3;

        if (availableLength <= 0) {
            return fileName.substring(0, maxLength - 3) + "...";
        }

        return name.substring(0, Math.min(name.length(), availableLength))
                + "..."
                + extension;
    }
}