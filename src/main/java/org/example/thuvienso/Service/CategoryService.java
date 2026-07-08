package org.example.thuvienso.Service;

import org.example.thuvienso.Dto.Request.CategoryRequest;
import org.example.thuvienso.Dto.Response.Category.CategoryResponse;
import org.example.thuvienso.Form.CategoryForm;
import org.example.thuvienso.Module.CategoryEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse getByIdResponse(String Id);
    CategoryEntity getById(String Id);

    List<CategoryResponse> getAll();
    void DeletedById(String id);
    CategoryResponse updateCategory(CategoryForm categoryForm,String id);
}
