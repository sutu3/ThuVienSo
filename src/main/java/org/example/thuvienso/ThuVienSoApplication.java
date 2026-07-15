package org.example.thuvienso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ThuVienSoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThuVienSoApplication.class, args);
    }

}
