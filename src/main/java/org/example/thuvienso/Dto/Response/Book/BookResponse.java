package org.example.thuvienso.Dto.Response.Book;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.example.thuvienso.Dto.Response.Category.CategoryResponse;
import org.example.thuvienso.Dto.Response.Document.DocumentResponse;
import org.example.thuvienso.Dto.Response.Document.DocumentResponseNoList;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookResponse {
    String idBook;
    String bookCode;
    String title;
    String author;
    String publisher;
    Integer publishYear;
    String shelfLocation;
    Integer totalCopies;
    Integer availableCopies;
    CategoryResponse categoryEntity;
    String thumbnail;
    DocumentResponseNoList document;
}