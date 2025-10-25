package com.brocode.service;

import com.brocode.entity.Order;
import com.brocode.entity.Supplier;
import com.brocode.repo.SupplierRepo;
import com.brocode.service.dto.OrderCreateDto;
import com.brocode.service.dto.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderMapper {
    private final OrderItemMapper itemMapper;
    private final SupplierRepo supplierRepo;

    public OrderResponseDto orderToResponse(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getSupplier().getId(),
                order.getOrderId(),
                order.getOrderDate(),
                order.getItems().stream().map(itemMapper::orderItemToResponse).toList(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getLastModifiedAt(),
                order.getCreatedBy(),
                order.getLastModifiedBy()
        );
    }

    public Order createToOrder(OrderCreateDto dto) {
        Supplier supplier = null;

        if (dto.supplierId() != null) supplier = supplierRepo.findById(dto.supplierId()).orElseThrow(() -> new NoSuchElementException("Order Not Found"));

        return Order.builder()
                .orderDate(dto.orderDate())
                .supplier(supplier)
                .totalAmount(dto.totalAmount())
                .build();
    }
}