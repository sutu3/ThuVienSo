package org.example.thuvienso.Service;

import org.example.thuvienso.Dto.Request.NewsRequest;
import org.example.thuvienso.Dto.Response.News.NewsResponse;
import org.springframework.data.domain.Page;

public interface NewsService {
    NewsResponse create(NewsRequest request);
    NewsResponse update(String id, NewsRequest request);
    NewsResponse getPublishedBySlug(String slug);
    Page<NewsResponse> getPublished(String keyword, String category, Boolean featured, int page, int size);
    Page<NewsResponse> getForAdmin(String keyword, String status, int page, int size);
    void delete(String id);
}
