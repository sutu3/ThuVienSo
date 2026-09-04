package org.example.thuvienso.Service.Impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Helper.GetUrl;
import org.example.thuvienso.Helper.LocalStorage;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TempQrServiceImpl {

    static final String TEMP_QR_PREFIX = "qrcodes/temp/";
    static final int SIZE = 300;

    LocalStorage localStorage;
    GetUrl getUrl;

    /** Nhận 1 đoạn text -> sinh ảnh QR -> trả về URL đầy đủ để hiển thị. */
    public String generateTempQr(String text) {
        if (text == null || text.isBlank()) {
            throw new AppException(ErrorCode.INVALID_KEY); // đổi mã lỗi cho phù hợp
        }
        if(text.length()>200){
            throw new AppException(ErrorCode.SIZE_TEXT_IS_TOO_BIG);

        }
        try {
            String objectName = TEMP_QR_PREFIX + UUID.randomUUID() + ".png";

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix matrix = new QRCodeWriter()
                    .encode(text, BarcodeFormat.QR_CODE, SIZE, SIZE, hints);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);

            String stored = localStorage.store(out.toByteArray(), objectName);
            return getUrl.getFileUrl(stored); // URL dạng http://<host>/files/raw/qrcodes/temp/<uuid>.png
        } catch (Exception e) {
            log.error("Không thể sinh QR tạm: {}", e.getMessage());
            throw new RuntimeException("Không thể sinh mã QR", e);
        }
    }
    public ResponseEntity<InputStreamResource> downloadTempQr(String objectName) {
        if (objectName == null || objectName.isBlank() || !localStorage.exists(objectName)) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }
        try {
            InputStream stream = localStorage.load(objectName);
            String fileName = Paths.get(objectName).getFileName().toString();
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            log.error("Không thể tải QR tạm: {}", e.getMessage());
            throw new RuntimeException("Không thể tải mã QR", e);
        }
    }
}