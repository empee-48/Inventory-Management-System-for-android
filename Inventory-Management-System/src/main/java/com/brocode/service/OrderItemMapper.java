package com.brocode.service;

import com.brocode.entity.Order;
import com.brocode.entity.OrderItem;
import com.brocode.entity.Product;
import com.brocode.repo.ProductRepo;
import com.brocode.service.dto.OrderItemCreateDto;
import com.brocode.service.dto.OrderItemResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderItemMapper {
    private final ProductRepo productRepo;

    public OrderItemResponseDto orderItemToResponse(OrderItem orderItem) {
        return new OrderItemResponseDto(
                orderItem.getId(),
                orderItem.getProduct().getId(),
                orderItem.getOrder().getId(),
                orderItem.getProduct().getName(),
                orderItem.getAmount(),
                orderItem.getOrderPrice(),
                orderItem.getCreatedAt(),
                orderItem.getLastModifiedAt(),
                orderItem.getCreatedBy(),
                orderItem.getLastModifiedBy()
        );
    }

    public OrderItem createToOrderItem(OrderItemCreateDto dto, Order order) {
        Product product = productRepo.findById(dto.productId()).orElseThrow(() -> new NoSuchElementException("Product Not Found"));

        return OrderItem.builder()
                .orderPrice(dto.orderPrice())
                .amount(dto.amount())
                .product(product)
                .order(order)
                .build();
    }
}