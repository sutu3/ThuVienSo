package org.example.thuvienso.Service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Request.DocumentRequest;
import org.example.thuvienso.Dto.Request.NewsRequest;
import org.example.thuvienso.Dto.Response.News.NewsResponse;
import org.example.thuvienso.Enum.StatusDocument;
import org.example.thuvienso.Enum.TypeDocument;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Helper.GetUrl;
import org.example.thuvienso.Helper.NewsContentImageProcessor;
import org.example.thuvienso.Helper.NewsHtmlThumbnailGenerator;
import org.example.thuvienso.Helper.NewsThumbnailExtractor;
import org.example.thuvienso.Mapper.CategoryMapper;
import org.example.thuvienso.Module.DocumentEntity;
import org.example.thuvienso.Repo.DocumentRepo;
import org.example.thuvienso.Service.CategoryService;
import org.example.thuvienso.Service.DocumentService;
import org.example.thuvienso.Service.MinioService;
import org.example.thuvienso.Service.NewsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Locale;

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
    private final NewsHtmlThumbnailGenerator newsHtmlThumbnailGenerator;
    MinioService minioService;
    DocumentService documentService;

    GetUrl getUrl;

    @Override
    @Transactional
    public NewsResponse create(NewsRequest request) {
        // 1. Upload ảnh nhúng base64 trong content -> thay src bằng URL nội bộ
        String content = newsContentImageProcessor.uploadEmbeddedImages(request.getContent());

        // 2. Sinh thumbnail từ HTML (đã thay src). baseUrl = host server nếu content dùng /files/raw/ tương đối
//        String thumbnail = newsHtmlThumbnailGenerator.generateFromHtml(
//                content, request.getTitle(), null);
//        log.warn("Generated thumbnail: {}", thumbnail);
        StatusDocument status = parseStatus(request.getStatus());

        DocumentEntity news = DocumentEntity.builder()
                .title(request.getTitle().trim())
                .content(content)
                .summary(resolveSummary(request.getSummary(), content))
                .slug(resolveSlug(request.getSlug(), request.getTitle(), null))
                .publishedAt(resolvePublishedAt(status, request.getPublishedAt(), null))
                .thumbnail(request.getThumbnail())
                .typeDocument(TypeDocument.ARTICLE)
                .status(status).viewCount(0L)
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
        StatusDocument status = StringUtils.hasText(request.getStatus()) ? parseStatus(request.getStatus()) : news.getStatus();
        news.setTitle(request.getTitle().trim());
        news.setContent(content);
        news.setSummary(resolveSummary(request.getSummary(), content));
        news.setSlug(resolveSlug(request.getSlug(), request.getTitle(), id));
        news.setPublishedAt(resolvePublishedAt(status, request.getPublishedAt(), news.getPublishedAt()));
        String oldThumbnail = news.getThumbnail();
        news.setThumbnail(newsHtmlThumbnailGenerator.generateFromHtml(content, request.getTitle(), null));
        news.setStatus(status);
        news.setCategoryEntity(categoryService.getById(request.getCategoryEntity()));
        NewsResponse response = toResponse(documentRepo.save(news));
        deleteOldGeneratedThumbnail(oldThumbnail, news.getThumbnail());
        return response;
    }

    @Override
    public NewsResponse getById(String idNew) {
        DocumentEntity document=documentService.getById(idNew);
        return toResponse(document);
    }

    @Override
    @Transactional
    public NewsResponse getPublishedBySlug(String slug) {
        DocumentEntity news = documentRepo.findBySlugAndTypeDocumentAndIsDeletedFalse(slug, TypeDocument.ARTICLE)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));
        if (news.getStatus() != StatusDocument.Approve || news.getPublishedAt() == null || news.getPublishedAt().isAfter(LocalDateTime.now())) throw new AppException(ErrorCode.NEWS_NOT_FOUND);
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
                ).and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("publishedAt"), LocalDateTime.now()));

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

    @Override
    public String uploadImage(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new AppException(ErrorCode.FILE_INVALID_TYPE);
        }
        // upload đã có sẵn: lưu file + trả previewUrl dạng http://<host>/files/raw/...
        return minioService.upload(file).getPreviewUrl();
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
        return PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("createdAt")));
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

    private String resolveSlug(String requestedSlug, String title, String currentId) {
        String source = StringUtils.hasText(requestedSlug) ? requestedSlug : title;
        String slug = Normalizer.normalize(source, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd').replace('Đ', 'd')
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (!StringUtils.hasText(slug)) throw new AppException(ErrorCode.INVALID_KEY);
        boolean exists = currentId == null ? documentRepo.existsBySlug(slug) : documentRepo.existsBySlugAndIdDocumentNot(slug, currentId);
        if (exists) throw new AppException(ErrorCode.NEWS_SLUG_EXISTS);
        return slug;
    }

    private String resolveSummary(String requestedSummary, String content) {
        if (StringUtils.hasText(requestedSummary)) return requestedSummary.trim();
        String text = org.jsoup.Jsoup.parse(content).text().trim();
        return text.length() <= 250 ? text : text.substring(0, 247) + "...";
    }

    private LocalDateTime resolvePublishedAt(StatusDocument status, LocalDateTime requested, LocalDateTime current) {
        if (requested != null) return requested;
        if (status == StatusDocument.Approve) return current != null ? current : LocalDateTime.now();
        return current;
    }

    private NewsResponse toResponse(DocumentEntity d) {
        return NewsResponse.builder().idNews(d.getIdDocument()).title(d.getTitle()).content(d.getContent()).summary(d.getSummary()).slug(d.getSlug()).publishedAt(d.getPublishedAt()).thumbnail(getUrl.getFileUrl(d.getThumbnail())).status(d.getStatus()).typeDocument(d.getTypeDocument()).categoryEntity(categoryMapper.toResponse(d.getCategoryEntity())).viewCount(d.getViewCount()).build();
    }
}
