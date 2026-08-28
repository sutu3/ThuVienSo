package org.example.thuvienso.Dto.Response.News;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.example.thuvienso.Dto.Response.Category.CategoryResponse;
import org.example.thuvienso.Enum.StatusDocument;
import org.example.thuvienso.Enum.TypeDocument;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsResponse {
    private String idNews, title, content, thumbnail;
    private StatusDocument status;
    private TypeDocument typeDocument;
    private CategoryResponse categoryEntity;
    private Long viewCount;
}
