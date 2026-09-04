package org.example.thuvienso.Controller;

import lombok.RequiredArgsConstructor;
import org.example.thuvienso.Dto.ApiResponse;
import org.example.thuvienso.Service.Impl.TempQrServiceImpl;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qr")
@RequiredArgsConstructor
public class TempQrController {

    private final TempQrServiceImpl tempQrService;

    @PostMapping("/generate")
    public ApiResponse<String> generate(@RequestBody String text) {
        return ApiResponse.<String>builder()
                .code(0).success(true)
                .message("Sinh mã QR thành công")
                .Result(tempQrService.generateTempQr(text))
                .build();
    }
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadTempQr(
            @RequestParam("object") String objectName) {
        return tempQrService.downloadTempQr(objectName);
    }
}