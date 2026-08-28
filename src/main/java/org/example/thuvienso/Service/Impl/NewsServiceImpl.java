package org.example.thuvienso.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.example.thuvienso.Dto.Request.NewsRequest;
import org.example.thuvienso.Dto.Response.News.NewsResponse;
import org.example.thuvienso.Enum.StatusDocument;
import org.example.thuvienso.Enum.TypeDocument;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Mapper.CategoryMapper;
import org.example.thuvienso.Module.DocumentEntity;
import org.example.thuvienso.Repo.DocumentRepo;
import org.example.thuvienso.Service.CategoryService;
import org.example.thuvienso.Service.NewsService;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {
    private final DocumentRepo documentRepo;
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @Override @Transactional
    public NewsResponse create(NewsRequest request) {
        DocumentEntity news = DocumentEntity.builder().title(request.getTitle().trim()).content(request.getContent()).thumbnail(request.getThumbnail()).typeDocument(TypeDocument.ARTICLE).status(parseStatus(request.getStatus())).viewCount(0L).downloadCount(0L).categoryEntity(categoryService.getById(request.getCategoryEntity())).isDeleted(false).build();
        return toResponse(documentRepo.save(news));
    }

    @Override @Transactional
    public NewsResponse update(String id, NewsRequest request) {
        DocumentEntity news = getArticle(id);
        news.setTitle(request.getTitle().trim()); news.setContent(request.getContent()); news.setThumbnail(request.getThumbnail()); if (StringUtils.hasText(request.getStatus())) news.setStatus(parseStatus(request.getStatus())); news.setCategoryEntity(categoryService.getById(request.getCategoryEntity()));
        return toResponse(documentRepo.save(news));
    }

    @Override @Transactional
    public NewsResponse getPublishedBySlug(String id) {
        DocumentEntity news = getArticle(id);
        if (news.getStatus() != StatusDocument.Approve) throw new AppException(ErrorCode.NEWS_NOT_FOUND);
        news.setViewCount((news.getViewCount() == null ? 0L : news.getViewCount()) + 1);
        return toResponse(news);
    }

    @Override @Transactional(readOnly = true)
    public Page<NewsResponse> getPublished(String keyword, String categoryId, Boolean ignoredFeatured, int page, int size) {
        Specification<DocumentEntity> spec = baseSpec().and((root, query, cb) -> cb.equal(root.get("status"), StatusDocument.Approve));
        if (StringUtils.hasText(keyword)) spec = spec.and(keywordSpec(keyword));
        if (StringUtils.hasText(categoryId)) spec = spec.and((root, query, cb) -> cb.equal(root.get("categoryEntity").get("idCategory"), categoryId));
        return documentRepo.findAll(spec, pageable(page, size)).map(this::toResponse);
    }

    @Override @Transactional(readOnly = true)
    public Page<NewsResponse> getForAdmin(String keyword, String status, int page, int size) {
        Specification<DocumentEntity> spec = baseSpec();
        if (StringUtils.hasText(keyword)) spec = spec.and(keywordSpec(keyword));
        if (StringUtils.hasText(status)) spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), parseStatus(status)));
        return documentRepo.findAll(spec, pageable(page, size)).map(this::toResponse);
    }

    @Override @Transactional
    public void delete(String id) { DocumentEntity news = getArticle(id); news.setIsDeleted(true); documentRepo.save(news); }

    private DocumentEntity getArticle(String id) { return documentRepo.findById(id).filter(d -> !d.getIsDeleted() && d.getTypeDocument() == TypeDocument.ARTICLE).orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND)); }
    private Specification<DocumentEntity> baseSpec() { return (root, query, cb) -> cb.and(cb.isFalse(root.get("isDeleted")), cb.equal(root.get("typeDocument"), TypeDocument.ARTICLE)); }
    private Specification<DocumentEntity> keywordSpec(String keyword) { String value = "%" + keyword.trim().toLowerCase() + "%"; return (root, query, cb) -> cb.or(cb.like(cb.lower(root.get("title")), value), cb.like(cb.lower(root.get("content")), value)); }
    private PageRequest pageable(int page, int size) { return PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by(Sort.Order.desc("createdAt"))); }
    private StatusDocument parseStatus(String status) { if (!StringUtils.hasText(status)) return StatusDocument.Pending; for (StatusDocument value : StatusDocument.values()) if (value.name().equalsIgnoreCase(status.trim())) return value; throw new AppException(ErrorCode.NEWS_INVALID_STATUS); }
    private NewsResponse toResponse(DocumentEntity d) { return NewsResponse.builder().idNews(d.getIdDocument()).title(d.getTitle()).content(d.getContent()).thumbnail(d.getThumbnail()).status(d.getStatus()).typeDocument(d.getTypeDocument()).categoryEntity(categoryMapper.toResponse(d.getCategoryEntity())).viewCount(d.getViewCount()).build(); }
}
