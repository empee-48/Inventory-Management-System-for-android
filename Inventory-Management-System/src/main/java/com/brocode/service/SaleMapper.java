package com.brocode.service;

import com.brocode.entity.Sale;
import com.brocode.entity.SaleItem;
import com.brocode.service.dto.SaleCreateDto;
import com.brocode.service.dto.SaleItemResponseDto;
import com.brocode.service.dto.SaleResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleMapper {
    private final SaleItemMapper mapper;

    public SaleResponseDto saleToResponse(Sale sale) {
        List<SaleItemResponseDto> items = null;

        if (sale.getItems() != null) items = sale.getItems().stream().map(mapper::saleItemToResponse).toList();
        return new SaleResponseDto(
                sale.getId(),
                sale.getSaleId(),
                sale.getSaleDate(),
                items,
                sale.getTotalAmount(),
                sale.getCreatedAt(),
                sale.getLastModifiedAt(),
                sale.getCreatedBy(),
                sale.getLastModifiedBy()
        );
    }

    public Sale createToSale(SaleCreateDto dto) {
        return Sale.builder()
                .saleDate(dto.saleDate())
                .totalAmount(dto.totalAmount())
                .build();
    }
}