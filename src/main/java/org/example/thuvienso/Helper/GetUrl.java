package org.example.thuvienso.Helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Module.DocumentEntity;
import org.example.thuvienso.Repo.DocumentRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetUrl {

    @Value("${storage.base-url}")
    private String baseUrl;
    private final DocumentRepo documentRepo;



    public String getFileUrl(String objectName) {
        if (objectName == null || objectName.isBlank()) return null;
        DocumentEntity document=documentRepo.findByObjectName(objectName)
                .orElseThrow(()-> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
        document.setViewCount((document.getViewCount() == null ? 0 : document.getViewCount()) + 1);
        documentRepo.save(document);
        return baseUrl.replaceAll("/+$", "") + "/" + objectName;
    }
}