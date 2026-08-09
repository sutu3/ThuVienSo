package org.example.thuvienso.Dto.Request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BorrowRequest {
    String idBook;
    Integer borrowDays; // số ngày mượn dự kiến (mặc định 14 nếu null)
    String note;
}