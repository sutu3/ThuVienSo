package org.example.thuvienso.Config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${mini.url}")
    private String url;

    @Value("${mini.access-key}")
    private String accessKey;

    @Value("${mini.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {

        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }
}
