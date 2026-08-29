package org.example.thuvienso.Service;

import org.example.thuvienso.Dto.Request.NewsRequest;
import org.example.thuvienso.Dto.Response.News.NewsResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface NewsService {
    NewsResponse create(NewsRequest request);
    NewsResponse update(String id, NewsRequest request);
    NewsResponse getById(String idNew);
    NewsResponse getPublishedBySlug(String slug);
     Page<NewsResponse> getPublished(
            String keyword,
            String categoryId,
            Boolean ignoredFeatured,
            int page,
            int size
    );
    Page<NewsResponse> getForAdmin(String keyword, String status, int page, int size);
    void delete(String id);
    String uploadImage(MultipartFile file) throws Exception;

}
