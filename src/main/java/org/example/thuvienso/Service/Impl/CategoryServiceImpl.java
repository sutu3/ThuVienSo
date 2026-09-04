package org.example.thuvienso.Service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Request.CategoryRequest;
import org.example.thuvienso.Dto.Response.Category.CategoryResponse;
import org.example.thuvienso.Enum.TypeDocument;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Form.CategoryForm;
import org.example.thuvienso.Mapper.CategoryMapper;
import org.example.thuvienso.Module.CategoryEntity;
import org.example.thuvienso.Repo.CategoryRepo;
import org.example.thuvienso.Service.CategoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {
    CategoryRepo categoryRepo;
    CategoryMapper categoryMapper;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        CategoryEntity categoryEntity = categoryMapper.toEntity(request);

        // set cha nếu có
        if (request.getParentCategory() != null && !request.getParentCategory().isBlank()) {
            CategoryEntity parent = getById(request.getParentCategory());
            categoryEntity.setParentCategory(parent);
            // chống trùng tên trong cùng 1 cha
            if (categoryRepo.existsByParentCategory_IdCategoryAndCategoryName(
                    parent.getIdCategory(), request.getCategoryName())) {
                throw new AppException(ErrorCode.CATEGORY_IS_EXIST);
            }
        } else if (categoryRepo.findByCategoryName(request.getCategoryName()).isPresent()) {
            // trùng tên ở cấp gốc
            throw new AppException(ErrorCode.CATEGORY_IS_EXIST);
        }

        categoryEntity.setIsDisplay(request.getIsDisplay() == null ? true : request.getIsDisplay());
        categoryEntity.setIsDeleted(false);
        categoryEntity.setCreatedAt(LocalDateTime.now());
        categoryRepo.save(categoryEntity);
        return toResponse(categoryEntity);
    }

    @Override
    public CategoryResponse getByIdResponse(String Id) {
        return categoryMapper.toResponse(getById(Id));
    }

    @Override
    public CategoryEntity getById(String idCategory) {
        CategoryEntity categoryEntity = categoryRepo.findById(idCategory)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        return categoryEntity;
    }

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepo.findAll().stream()
                .filter(categoryEntity -> !categoryEntity.getIsDeleted()||categoryEntity.getIsDisplay())
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void DeletedById(String id) {
        CategoryEntity category = getById(id);
        category.setIsDeleted(true);
        category.setDeletedAt(LocalDateTime.now());
        categoryRepo.save(category);
    }

    @Override
    public CategoryResponse updateCategory(CategoryForm categoryForm, String id) {
        CategoryEntity categoryEntity = getById(id);
        categoryMapper.update(categoryEntity, categoryForm);
        categoryEntity.setUpdatedAt(LocalDateTime.now());
        categoryRepo.save(categoryEntity);
        return categoryMapper.toResponse(categoryEntity);
    }

    @Override
    public List<CategoryResponse> getCategoriesByDocumentType(String type) {
        TypeDocument typeDocument = TypeDocument.valueOf(type.toUpperCase());
        return categoryRepo.findCategoriesByDocumentType(typeDocument).stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getTree() {
        return categoryRepo.findByParentCategoryIsNull().stream()
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .filter(categoryEntity -> !categoryEntity.getIsDeleted()||categoryEntity.getIsDisplay())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getCategoryChildren(String parentId) {
        return categoryRepo.findByParentCategory_IdCategory(parentId).stream()
                .filter(categoryEntity -> !categoryEntity.getIsDeleted()||categoryEntity.getIsDisplay())
                .map(this::toResponse)      // chỉ 1 cấp, không đệ quy
                .collect(Collectors.toList());
    }
    private CategoryResponse toResponse(CategoryEntity c) {
        return categoryMapper.toResponse(c);
    }


}
