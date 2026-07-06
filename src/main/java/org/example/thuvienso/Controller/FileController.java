package org.example.thuvienso.Controller;

import io.minio.errors.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.thuvienso.Dto.ApiResponse;
import org.example.thuvienso.Dto.Response.File.FileResponse;
import org.example.thuvienso.Service.FileService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {


    FileService fileService;

    @PostMapping(
            value = "/upload",
            consumes = "multipart/form-data"
    )
    public ApiResponse<FileResponse> uploadFile(
            @RequestPart("file") MultipartFile file,
            @PathVariable("idDocument") String idDocument
    ) throws Exception {

        return ApiResponse.<FileResponse>builder()
                .success(true)
                .message("Upload file thành công")
                .Result(fileService.uploadFile(file,idDocument))
                .build();
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<InputStreamResource> viewFile(
            @PathVariable String id
    ) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {

        return fileService.viewFile(id);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> downloadFile(
            @PathVariable String id
    ) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {

        return fileService.dowloadFile(id);
    }


}
