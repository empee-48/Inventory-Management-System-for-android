package com.brocode.service;

import com.brocode.entity.Category;
import com.brocode.service.dto.CategoryCreateDto;
import com.brocode.service.dto.CategoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MyCategoryMapper {
    private final ProductMapper productMapper;

    public CategoryResponseDto categoryToResponse(Category category) {
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getProducts().stream().map(productMapper::productToResponse).toList(),
                category.getCreatedAt(),
                category.getLastModifiedAt(),
                category.getCreatedBy(),
                category.getLastModifiedBy()
        );
    }

    public Category createToCategory(CategoryCreateDto dto) {
        return Category.builder()
                .name(dto.name())
                .build();
    }
}
