package org.example.thuvienso.Helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

@Component
@Slf4j
public class TempQrCleanupJob {

    @Value("${storage.location}")
    private String location;

    // Chạy lúc 23:59 mỗi ngày, xóa toàn bộ ảnh QR tạm trong qrcodes/temp/
    @Scheduled(cron = "0 59 23 * * *")
    public void cleanupTempQr() {
        Path dir = Paths.get(location).resolve("qrcodes/temp").normalize();
        if (!Files.exists(dir)) return;

        try (Stream<Path> files = Files.list(dir)) {
            files.filter(Files::isRegularFile).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                    log.info("Đã xóa QR tạm: {}", p.getFileName());
                } catch (IOException e) {
                    log.error("Lỗi xóa QR tạm {}: {}", p, e.getMessage());
                }
            });
        } catch (IOException e) {
            log.error("Lỗi quét thư mục QR tạm: {}", e.getMessage());
        }
    }
}