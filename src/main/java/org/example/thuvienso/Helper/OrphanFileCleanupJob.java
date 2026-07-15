package org.example.thuvienso.Helper;


import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Module.FileEntity;
import org.example.thuvienso.Repo.FileRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrphanFileCleanupJob {

    private final FileRepo fileRepo;
    private final MinioClient minioClient;

    // Chạy mỗi giờ, xóa file mồ côi tạo cách đây > 24h
    @Scheduled(cron = "0 0 * * * *")
    public void cleanup() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<FileEntity> orphans = fileRepo.findByDocumentEntityIsNullAndCreatedAtBefore(threshold);

        for (FileEntity f : orphans) {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket("thuvienso")
                        .object(f.getPartFile())
                        .build());
                fileRepo.delete(f);
                log.info("Đã dọn file mồ côi: {}", f.getPartFile());
            } catch (Exception e) {
                log.error("Lỗi dọn file mồ côi {}: {}", f.getPartFile(), e.getMessage());
            }
        }
    }
}
