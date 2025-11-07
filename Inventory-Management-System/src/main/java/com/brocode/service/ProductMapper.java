package com.brocode.service;

import com.brocode.entity.Category;
import com.brocode.entity.Product;
import com.brocode.repo.CategoryRepo;
import com.brocode.service.dto.ProductCreateDto;
import com.brocode.service.dto.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductMapper {
    private final CategoryRepo categoryRepo;

    public ProductResponseDto productToResponse(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getProductKey(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getInStock(),
                product.getWarningStockLevel(),
                product.getWarningStockLevel() >= product.getInStock(),
                product.getUnit(),
                product.getCreatedAt(),
                product.getLastModifiedAt(),
                product.getCreatedBy(),
                product.getLastModifiedBy()
        );
    }

    public Product createToProduct(ProductCreateDto dto) {
        Category category = categoryRepo.findById(dto.categoryId()).orElseThrow(() -> new NoSuchElementException("Category Not Found"));
        return Product.builder()
                .name(dto.name())
                .description(dto.description())
                .inStock(dto.inStock())
                .price(dto.price())
                .unit(dto.unit())
                .category(category)
                .warningStockLevel(dto.warningStockLevel())
                .build();
    }
}