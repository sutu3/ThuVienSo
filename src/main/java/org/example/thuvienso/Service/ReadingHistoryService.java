package org.example.thuvienso.Service;

import org.example.thuvienso.Dto.Response.Book.BookResponse;

import java.util.List;

public interface ReadingHistoryService {
    void markRead(String idBook);

    List<BookResponse> getMyHistory();
}