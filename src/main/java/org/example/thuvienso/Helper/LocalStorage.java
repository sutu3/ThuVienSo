package org.example.thuvienso.Helper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocalStorage {

    @Value("${storage.location}")
    private String location;

    private Path root;

    @PostConstruct
    public void init() throws IOException {
        root = Paths.get(location).toAbsolutePath().normalize();
        Files.createDirectories(root);
        log.info("Storage root: {}", root);
    }

    private Path resolve(String objectName) {
        Path target = root.resolve(objectName).normalize();
        if (!target.startsWith(root)) {              // chống path traversal
            throw new IllegalArgumentException("Invalid object name: " + objectName);
        }
        return target;
    }

    public String store(byte[] bytes, String objectName) throws IOException {
        Path target = resolve(objectName);
        Files.createDirectories(target.getParent());
        Files.write(target, bytes);
        return objectName;
    }

    public InputStream load(String objectName) throws IOException {
        return Files.newInputStream(resolve(objectName), StandardOpenOption.READ);
    }

    public long size(String objectName) throws IOException {
        return Files.size(resolve(objectName));
    }

    public boolean exists(String objectName) {
        return Files.exists(resolve(objectName));
    }

    public void delete(String objectName) throws IOException {
        Files.deleteIfExists(resolve(objectName));
    }

    /** Đọc 1 khoảng [start, start+length) để hỗ trợ Range (video/audio). */
    public InputStream loadRange(String objectName, long start, long length) throws IOException {
        InputStream in = Files.newInputStream(resolve(objectName));
        long skipped = 0;
        while (skipped < start) {
            long s = in.skip(start - skipped);
            if (s <= 0) break;
            skipped += s;
        }
        return new LimitedInputStream(in, length);
    }

    /** InputStream chỉ đọc tối đa `limit` byte. */
    private static class LimitedInputStream extends FilterInputStream {
        private long remaining;
        LimitedInputStream(InputStream in, long limit) { super(in); this.remaining = limit; }
        @Override public int read() throws IOException {
            if (remaining <= 0) return -1;
            int b = super.read();
            if (b >= 0) remaining--;
            return b;
        }
        @Override public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) return -1;
            int toRead = (int) Math.min(len, remaining);
            int n = super.read(b, off, toRead);
            if (n > 0) remaining -= n;
            return n;
        }
    }
}