package org.example.thuvienso.Helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component
@Slf4j
public class DatabaseBackupJob {

    // Lấy thông tin DB từ cấu hình datasource sẵn có
    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    @Value("${spring.datasource.username}")
    private String dbUser;
    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${backup.dir}")
    private String backupDir;
    @Value("${backup.mysqldump-path:mysqldump}")
    private String mysqldumpPath;
    @Value("${backup.keep:0}")
    private int keep;

    // Giây Phút Giờ Ngày Tháng Thứ  -> 02:00 ngày 1 của tháng 1,4,7,10 (mỗi 3 tháng)
    @Scheduled(cron = "0 0 2 1 1,4,7,10 *", zone = "Asia/Ho_Chi_Minh")
    public void scheduledBackup() {
        try {
            String path = backup();
            log.info("Backup DB định kỳ thành công: {}", path);
        } catch (Exception e) {
            log.error("Backup DB định kỳ thất bại: {}", e.getMessage(), e);
        }
    }

    /** Thực hiện dump DB ra file .sql, trả về đường dẫn file. */
    public String backup() throws IOException, InterruptedException {
        String host = extract(datasourceUrl, "://", ":", "localhost");
        String port = extractPort(datasourceUrl);
        String dbName = extractDbName(datasourceUrl);

        Path dir = Paths.get(backupDir);
        Files.createDirectories(dir);

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path outFile = dir.resolve("backup_" + dbName + "_" + timestamp + ".sql");

        ProcessBuilder pb = new ProcessBuilder(
                mysqldumpPath,
                "-h", host,
                "-P", port,
                "-u", dbUser,
                "-p" + dbPassword,          // KHÔNG có space giữa -p và password
                "--databases", dbName,
                "--add-drop-table",
                "--routines",
                "--events"
        );
        pb.redirectErrorStream(false);
        pb.redirectOutput(outFile.toFile());          // ghi stdout thẳng vào file .sql
        File errFile = dir.resolve("backup_" + timestamp + ".err").toFile();
        pb.redirectError(errFile);

        log.info("Bắt đầu mysqldump DB '{}' -> {}", dbName, outFile);
        Process process = pb.start();
        int exit = process.waitFor();

        if (exit != 0) {
            String err = Files.exists(errFile.toPath())
                    ? Files.readString(errFile.toPath()) : "(no error output)";
            throw new IOException("mysqldump thất bại (exit=" + exit + "): " + err);
        }
        errFile.delete();   // dump ok thì bỏ file lỗi rỗng

        cleanupOld(dir);
        return outFile.toString();
    }

    /** Giữ lại `keep` bản mới nhất, xóa các bản cũ hơn (keep <= 0 = giữ tất cả). */
    private void cleanupOld(Path dir) {
        if (keep <= 0) return;
        try (Stream<Path> files = Files.list(dir)) {
            List<Path> backups = files
                    .filter(p -> p.getFileName().toString().endsWith(".sql"))
                    .sorted(Comparator.comparingLong(p -> -p.toFile().lastModified()))
                    .toList();
            for (int i = keep; i < backups.size(); i++) {
                try {
                    Files.deleteIfExists(backups.get(i));
                    log.info("Đã xóa backup cũ: {}", backups.get(i));
                } catch (IOException e) {
                    log.warn("Không xóa được backup cũ {}: {}", backups.get(i), e.getMessage());
                }
            }
        } catch (IOException e) {
            log.warn("Không dọn được backup cũ: {}", e.getMessage());
        }
    }

    // ---- Tách host/port/dbName từ JDBC url: jdbc:mysql://localhost:3306/thuvienso?... ----
    private String extract(String url, String after, String before, String def) {
        int a = url.indexOf(after);
        if (a < 0) return def;
        String rest = url.substring(a + after.length());
        int b = rest.indexOf(before);
        return b < 0 ? rest : rest.substring(0, b);
    }

    private String extractPort(String url) {
        String afterHost = url.substring(url.indexOf("://") + 3);
        int colon = afterHost.indexOf(':');
        if (colon < 0) return "3306";
        String rest = afterHost.substring(colon + 1);
        int slash = rest.indexOf('/');
        return slash < 0 ? rest : rest.substring(0, slash);
    }

    private String extractDbName(String url) {
        String afterHost = url.substring(url.indexOf("://") + 3);
        int slash = afterHost.indexOf('/');
        if (slash < 0) return "thuvienso";
        String rest = afterHost.substring(slash + 1);
        int q = rest.indexOf('?');
        return q < 0 ? rest : rest.substring(0, q);
    }
}