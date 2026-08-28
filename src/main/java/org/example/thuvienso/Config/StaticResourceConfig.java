package org.example.thuvienso.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${storage.location}")
    private String storagePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        System.out.println(">>> STORAGE PATH = " + storagePath);

        registry.addResourceHandler("/files/raw/**")
                .addResourceLocations(
                        "file:" + storagePath + "/"
                );
    }
}
