package com.brocode.service;

import com.brocode.entity.Product;
import com.brocode.entity.Sale;
import com.brocode.entity.SaleItem;
import com.brocode.repo.ProductRepo;
import com.brocode.repo.SalesRepo;
import com.brocode.service.dto.SaleItemCreateDto;
import com.brocode.service.dto.SaleItemResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SaleItemMapper {
    private final ProductRepo productRepo;

    public SaleItemResponseDto saleItemToResponse(SaleItem saleItem) {
        return new SaleItemResponseDto(
                saleItem.getId(),
                saleItem.getProduct().getId(),
                saleItem.getSale().getId(),
                saleItem.getProduct().getName(),
                saleItem.getAmount(),
                saleItem.getSalePrice(),
                saleItem.getCreatedAt(),
                saleItem.getLastModifiedAt(),
                saleItem.getCreatedBy(),
                saleItem.getLastModifiedBy()
        );
    }

    public SaleItem createToSaleItem(SaleItemCreateDto dto, Sale sale) {
        Product product = productRepo.findById(dto.productId()).orElseThrow(() -> new NoSuchElementException("Product Not Found"));

        return SaleItem.builder()
                .amount(dto.amount())
                .salePrice(dto.price())
                .sale(sale)
                .product(product)
                .build();
    }
}