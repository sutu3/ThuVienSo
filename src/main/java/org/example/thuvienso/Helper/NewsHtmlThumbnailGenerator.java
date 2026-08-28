package org.example.thuvienso.Helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.swing.Java2DRenderer;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsHtmlThumbnailGenerator {

    private static final String PREFIX = "thumbnails/news/";
    private static final int RENDER_WIDTH = 800;   // chiều rộng "trang" để layout
    private static final int RENDER_HEIGHT = 1000; // chiều cao cắt tối đa
    private static final int THUMB_WIDTH = 400;
    private static final int THUMB_HEIGHT = 500;

    private final LocalStorage localStorage;

    /** baseUrl: dùng để resolve <img src> tương đối; có thể null nếu content chỉ có URL tuyệt đối. */
    public String generateFromHtml(String rawHtml, String title, String baseUrl) {
        try {
            String xhtml = toXhtmlDocument(rawHtml, title);

            org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(xhtml);
            jsoupDoc.outputSettings().syntax(Syntax.xml);
            org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);

            Java2DRenderer renderer = (baseUrl == null)
                    ? new Java2DRenderer(w3cDoc, RENDER_WIDTH, RENDER_HEIGHT)
                    : new Java2DRenderer(w3cDoc, baseUrl, RENDER_WIDTH, RENDER_HEIGHT);

            BufferedImage full = renderer.getImage();
            if (full == null) return null;

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.of(full)
                    .size(THUMB_WIDTH, THUMB_HEIGHT)
                    .outputFormat("jpg")
                    .toOutputStream(out);

            String objectName = PREFIX + UUID.randomUUID() + ".jpg";
            return localStorage.store(out.toByteArray(), objectName);
        } catch (Exception e) {
            log.warn("Không render được thumbnail từ HTML", e);
            return null; // không chặn tạo bài viết
        }
    }

    /** Bọc content vào 1 tài liệu XHTML hợp lệ + CSS cơ bản để layout đẹp. */
    private String toXhtmlDocument(String rawHtml, String title) {
        String safeTitle = title == null ? "" : title;
        String body = rawHtml == null ? "" : rawHtml;
        String html = "<html><head><style>"
                + "body{font-family:sans-serif;margin:24px;color:#111;}"
                + "h1{font-size:22px;margin-bottom:12px;}"
                + "p{font-size:15px;line-height:1.5;margin:8px 0;}"
                + "img{max-width:100%;height:auto;}"
                + "ul{margin:8px 0 8px 20px;}"
                + "</style></head><body>"
                + "<h1>" + escape(safeTitle) + "</h1>"
                + body
                + "</body></html>";
        // jsoup tự sửa thẻ chưa đóng, rồi ép xuất XML (XHTML) cho Flying Saucer
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        doc.outputSettings().syntax(Syntax.xml).escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
        return doc.html();
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}