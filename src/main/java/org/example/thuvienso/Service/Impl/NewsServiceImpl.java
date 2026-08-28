package org.example.thuvienso.Service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Request.NewsRequest;
import org.example.thuvienso.Dto.Response.Document.DocumentResponse;
import org.example.thuvienso.Dto.Response.News.NewsResponse;
import org.example.thuvienso.Enum.StatusDocument;
import org.example.thuvienso.Enum.TypeDocument;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Helper.GetUrl;
import org.example.thuvienso.Helper.NewsContentImageProcessor;
import org.example.thuvienso.Helper.NewsThumbnailExtractor;
import org.example.thuvienso.Mapper.CategoryMapper;
import org.example.thuvienso.Mapper.DocumentMapper;
import org.example.thuvienso.Module.DocumentEntity;
import org.example.thuvienso.Repo.DocumentRepo;
import org.example.thuvienso.Service.CategoryService;
import org.example.thuvienso.Service.NewsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NewsServiceImpl implements NewsService {
    private final DocumentRepo documentRepo;
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;
    private final NewsThumbnailExtractor newsThumbnailExtractor;
    private final NewsContentImageProcessor newsContentImageProcessor;
    GetUrl getUrl;

    @Override
    @Transactional
    public NewsResponse create(NewsRequest request) {
        String content = newsContentImageProcessor.uploadEmbeddedImages(request.getContent());
        DocumentEntity news = DocumentEntity.builder()
                .title(request.getTitle().trim())
                .content(content)
                .thumbnail(newsThumbnailExtractor.generate(content))
                .typeDocument(TypeDocument.ARTICLE)
                .status(parseStatus(request.getStatus())).viewCount(0L)
                .downloadCount(0L)
                .categoryEntity(categoryService.getById(request.getCategoryEntity()))
                .isDeleted(false).build();
        return toResponse(documentRepo.save(news));
    }

    @Override
    @Transactional
    public NewsResponse update(String id, NewsRequest request) {
        DocumentEntity news = getArticle(id);
        String content = newsContentImageProcessor.uploadEmbeddedImages(request.getContent());
        news.setTitle(request.getTitle().trim());
        news.setContent(content);
        String oldThumbnail = news.getThumbnail();
        news.setThumbnail(newsThumbnailExtractor.generate(content));
        if (StringUtils.hasText(request.getStatus())) news.setStatus(parseStatus(request.getStatus()));
        news.setCategoryEntity(categoryService.getById(request.getCategoryEntity()));
        NewsResponse response = toResponse(documentRepo.save(news));
        deleteOldGeneratedThumbnail(oldThumbnail, news.getThumbnail());
        return response;
    }

    @Override
    @Transactional
    public NewsResponse getPublishedBySlug(String id) {
        DocumentEntity news = getArticle(id);
        if (news.getStatus() != StatusDocument.Approve) throw new AppException(ErrorCode.NEWS_NOT_FOUND);
        news.setViewCount((news.getViewCount() == null ? 0L : news.getViewCount()) + 1);
        return toResponse(news);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NewsResponse> getPublished(
            String keyword,
            String categoryId,
            Boolean ignoredFeatured,
            int page,
            int size
    ) {
        Specification<DocumentEntity> spec = baseSpec()
                .and((root, query, cb) ->
                        cb.equal(root.get("status"), StatusDocument.Approve)
                );

        if (StringUtils.hasText(keyword)) {
            spec = spec.and(keywordSpec(keyword));
        }

        if (StringUtils.hasText(categoryId)) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(
                            root.get("categoryEntity").get("idCategory"),
                            categoryId
                    )
            );
        }

        return documentRepo.findAll(spec, pageable(page, size)).map(this::toResponse);
    }
    @Override
    @Transactional(readOnly = true)
    public Page<NewsResponse> getForAdmin(String keyword, String status, int page, int size) {
        Specification<DocumentEntity> spec = baseSpec();
        if (StringUtils.hasText(keyword)) spec = spec.and(keywordSpec(keyword));
        if (StringUtils.hasText(status))
            spec = spec.and((root, query, cb)
                    -> cb.equal(root.get("status"), parseStatus(status)));
        return documentRepo.findAll(spec, pageable(page, size)).map(this::toResponse);
    }

    @Override
    @Transactional
    public void delete(String id) {
        DocumentEntity news = getArticle(id);
        news.setIsDeleted(true);
        documentRepo.save(news);
    }

    private DocumentEntity getArticle(String id) {
        return documentRepo.findById(id)
                .filter(d -> !d.getIsDeleted() && d.getTypeDocument() == TypeDocument.ARTICLE)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));
    }

    private Specification<DocumentEntity> baseSpec() {
        return (root, query, cb) -> cb.and(cb.isFalse(root.get("isDeleted")), cb.equal(root.get("typeDocument"), TypeDocument.ARTICLE));
    }

    private Specification<DocumentEntity> keywordSpec(String keyword) {
        String value = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(cb.like(cb.lower(root.get("title")), value), cb.like(cb.lower(root.get("content")), value));
    }

    private PageRequest pageable(int page, int size) {
        return PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by(Sort.Order.desc("createdAt")));
    }

    private StatusDocument parseStatus(String status) {
        if (!StringUtils.hasText(status)) return StatusDocument.Pending;
        for (StatusDocument value : StatusDocument.values())
            if (value.name().equalsIgnoreCase(status.trim())) return value;
        throw new AppException(ErrorCode.NEWS_INVALID_STATUS);
    }

    private void deleteOldGeneratedThumbnail(String oldThumbnail, String newThumbnail) {
        if (!StringUtils.hasText(oldThumbnail) || oldThumbnail.equals(newThumbnail) || !oldThumbnail.endsWith("_news-thumbnail.jpg")) return;
        try { newsThumbnailExtractor.delete(oldThumbnail); } catch (Exception e) { log.warn("Không thể xóa thumbnail cũ: {}", oldThumbnail, e); }
    }

    private NewsResponse toResponse(DocumentEntity d) {
        return NewsResponse.builder().idNews(d.getIdDocument()).title(d.getTitle()).content(d.getContent()).thumbnail(getUrl.getFileUrl(d.getThumbnail())).status(d.getStatus()).typeDocument(d.getTypeDocument()).categoryEntity(categoryMapper.toResponse(d.getCategoryEntity())).viewCount(d.getViewCount()).build();
    }
}
