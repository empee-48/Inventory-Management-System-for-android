package com.brocode.service;

import com.brocode.entity.Category;
import com.brocode.service.dto.CategoryCreateDto;
import com.brocode.service.dto.CategoryResponseDto;
import com.brocode.service.dto.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class MyCategoryMapper {
    private final ProductMapper productMapper;

    public CategoryResponseDto categoryToResponse(Category category) {
        List<ProductResponseDto> products = null;

        if (category.getProducts() != null) products = category.getProducts().stream().map(productMapper::productToResponse).toList();
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                products,
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
