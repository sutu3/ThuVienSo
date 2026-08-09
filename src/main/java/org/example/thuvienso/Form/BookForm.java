package org.example.thuvienso.Form;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookForm {
    String isbn;
    String title;
    String author;
    String publisher;
    Integer publishYear;
    String shelfLocation;
    Integer totalCopies;
    String thumbnail;
}