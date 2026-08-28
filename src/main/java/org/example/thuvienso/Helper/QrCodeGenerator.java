package org.example.thuvienso.Helper;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QrCodeGenerator {

    static final String QR_PREFIX = "qrcodes/";
    static final int SIZE = 300;

    LocalStorage localStorage;

    /**
     * Sinh ảnh QR mã hoá `content` (ở đây là idBook), lưu ra đĩa,
     * trả về objectName dạng "qrcodes/<idBook>.png".
     */
    public String generate(String content) {
        try {
            String objectName = QR_PREFIX + content + ".png";

            // Đã sinh trước đó -> tái dùng, không ghi lại
            if (localStorage.exists(objectName)) {
                return objectName;
            }

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix matrix = new QRCodeWriter()
                    .encode(content, BarcodeFormat.QR_CODE, SIZE, SIZE, hints);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);

            return localStorage.store(out.toByteArray(), objectName);
        } catch (Exception e) {
            // Không chặn luồng getAll nếu QR lỗi
            return null;
        }
    }
}