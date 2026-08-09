package org.example.thuvienso.Dto.Response.Book;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

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
    String thumbnail;
}