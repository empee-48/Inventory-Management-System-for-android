package com.brocode.service;

import com.brocode.entity.Batch;
import com.brocode.entity.Order;
import com.brocode.entity.Product;
import com.brocode.repo.OrderRepo;
import com.brocode.repo.ProductRepo;
import com.brocode.repo.SaleItemRepo;
import com.brocode.service.dto.BatchResponseDto;
import com.brocode.service.dto.OrderItemCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BatchMapper {
    private final ProductRepo productRepo;
    private final OrderRepo orderRepo;
    private final SaleItemRepo saleItemRepo;
    private final SaleItemMapper saleItemMapper;

    public Batch createToBatch(OrderItemCreateDto item, Order order){
        Product product = productRepo.findById(item.productId()).orElseThrow(() -> new NoSuchElementException("Product Not Found"));

        return Batch.builder()
                .stockLeft(item.amount())
                .orderPrice(item.orderPrice())
                .order(order)
                .product(product)
                .build();
    }

    public BatchResponseDto batchToResponse(Batch batch){
        return new BatchResponseDto(
                batch.getId(),
                batch.getOrder().getId(),
                batch.getProduct().getId(),
                batch.getProduct().getName(),
                batch.getSales().stream().map(saleItemMapper::saleItemToResponse).toList(),
                batch.getStockLeft(),
                batch.getOrder().getOrderDate(),
                batch.getCreatedBy(),
                batch.getLastModifiedBy(),
                batch.getCreatedAt(),
                batch.getLastModifiedAt()
        );
    }
}
