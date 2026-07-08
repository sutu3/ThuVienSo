package org.example.thuvienso.Service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Request.CategoryRequest;
import org.example.thuvienso.Dto.Response.Category.CategoryResponse;
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

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepo categoryRepo;
    CategoryMapper categoryMapper;
    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        CategoryEntity categoryEntity=categoryMapper.toEntity(request);
        if(categoryRepo.findByCategoryName(request.getCategoryName())==null){
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        categoryEntity.setIsDeleted(false);
        categoryEntity.setCreatedAt(LocalDateTime.now());
        return categoryMapper.toResponse(categoryRepo.save(categoryEntity));
    }

    @Override
    public CategoryResponse getByIdResponse(String Id) {
        return categoryMapper.toResponse(getById(Id));
    }

    @Override
    public CategoryEntity getById(String idCategory) {
        CategoryEntity categoryEntity=categoryRepo.findById(idCategory)
                .orElseThrow(()->new AppException(ErrorCode.ROLE_NOT_FOUND));

        return categoryEntity;
    }

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepo.findAll().stream()
                .filter(categoryEntity -> !categoryEntity.getIsDeleted())
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void DeletedById(String id) {
        CategoryEntity category=getById(id);
        category.setIsDeleted(true);
        category.setDeletedAt(LocalDateTime.now());
        categoryRepo.save(category);
    }

    @Override
    public CategoryResponse updateCategory(CategoryForm categoryForm, String id) {
        CategoryEntity categoryEntity=getById(id);
        categoryMapper.update(categoryEntity,categoryForm);
        categoryEntity.setUpdatedAt(LocalDateTime.now());
        categoryRepo.save(categoryEntity);
        return categoryMapper.toResponse(categoryEntity);
    }
}
