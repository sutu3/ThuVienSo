package org.example.thuvienso.Service;

import org.example.thuvienso.Dto.Request.BookRequest;
import org.example.thuvienso.Dto.Response.Book.BookResponse;
import org.example.thuvienso.Form.BookForm;
import org.example.thuvienso.Module.BookEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BookService {
    BookResponse createBook(BookRequest request);

    BookResponse getByIdResponse(String id);

    BookEntity getById(String id);

    BookResponse getByBookCode(String bookCode); // phục vụ tra cứu QR

    List<BookResponse> getAll();

    void deletedById(String id);

    BookResponse updateBook(BookForm form, String id);
}