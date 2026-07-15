package org.example.thuvienso.Dto.Request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.thuvienso.Enum.StatusDocument;
import org.example.thuvienso.Enum.TypeDocument;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentSearchRequest {
    String keyword;
    TypeDocument typeDocument;
    StatusDocument status;
    String categoryId;
    LocalDateTime fromDate;
    LocalDateTime toDate;
}
