package org.example.thuvienso.Dto.Response.Borrow;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.thuvienso.Enum.BorrowStatus;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BorrowResponse {
    String idBorrow;
    String idBook;
    String bookTitle;
    String idAccount;
    String borrowerName;
    BorrowStatus status;
    LocalDate borrowDate;
    LocalDate dueDate;
    LocalDate returnDate;
    String note;
}