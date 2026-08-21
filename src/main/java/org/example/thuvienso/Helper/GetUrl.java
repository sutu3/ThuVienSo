package org.example.thuvienso.Helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GetUrl {

    @Value("${storage.base-url}")
    private String baseUrl;

    public String getFileUrl(String objectName) {
        if (objectName == null || objectName.isBlank()) return null;
        return baseUrl.replaceAll("/+$", "") + "/" + objectName;
    }
}