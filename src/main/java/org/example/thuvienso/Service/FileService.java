package org.example.thuvienso.Service;

import io.minio.errors.*;
import org.example.thuvienso.Dto.Request.FileRequest;
import org.example.thuvienso.Dto.Response.File.FileResponse;
import org.example.thuvienso.Module.FileEntity;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public interface FileService {
    FileResponse uploadFile(MultipartFile file,String idDocument) throws Exception;
    ResponseEntity<InputStreamResource> viewFile(String id) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;
    ResponseEntity<InputStreamResource> dowloadFile(String id) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;
    FileEntity getByFileName(String fileName);
    List<FileResponse> getByIdDocument(String idDocument);
    void deleteFile(String id) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

}
