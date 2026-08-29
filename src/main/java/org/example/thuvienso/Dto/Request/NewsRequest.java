package org.example.thuvienso.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NewsRequest {
    @NotBlank(message = "INVALID_KEY")
    @Size(max = 256, message = "INVALID_KEY")
    private String title;
    @NotBlank(message = "INVALID_KEY")
    private String content;
    @Size(max = 500, message = "INVALID_KEY")
    private String summary;
    @Size(max = 280, message = "INVALID_KEY")
    private String slug;
    private LocalDateTime publishedAt;
    @NotBlank(message = "INVALID_KEY")
    private String categoryEntity;
    private String status;
    private String thumbnail;
}
