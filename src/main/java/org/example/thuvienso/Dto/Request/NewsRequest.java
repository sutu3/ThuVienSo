package org.example.thuvienso.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewsRequest {
    @NotBlank(message = "INVALID_KEY")
    @Size(max = 256, message = "INVALID_KEY")
    private String title;
    @NotBlank(message = "INVALID_KEY")
    private String content;
    @NotBlank(message = "INVALID_KEY")
    private String categoryEntity;
    private String status;
}
