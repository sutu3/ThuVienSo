package org.example.thuvienso.Service;

import org.example.thuvienso.Dto.Response.Book.BookResponse;
import java.util.List;

public interface FavoriteService {
    void addFavorite(String idBook);
    void removeFavorite(String idBook);
    List<BookResponse> getMyFavorites();
}