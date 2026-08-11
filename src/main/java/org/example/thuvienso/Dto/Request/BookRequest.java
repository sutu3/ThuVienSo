package org.example.thuvienso.Dto.Request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookRequest {
    String bookCode;
    String title;
    String author;
    String publisher;
    Integer publishYear;
    String shelfLocation;
    Integer totalCopies;
    String categoryEntity; // idCategory
}