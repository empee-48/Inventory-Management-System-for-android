package com.brocode.controller;

import com.brocode.entity.Order;
import com.brocode.service.MyOrderService;
import com.brocode.service.dto.OrderCreateDto;
import com.brocode.service.dto.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/inventory/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final MyOrderService service;

    @GetMapping
    public ResponseEntity<?> getCategory(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false)LocalDate orderDate,
            @RequestParam(required = false) Double lowerBoundaryAmount,
            @RequestParam(required = false) Double higherBoundaryAmount
    ){
        if (id != null) return ResponseEntity.ok(service.getOrder(id));

        List<OrderResponseDto> orders = service.getAll().reversed();

        if (categoryId != null) orders = orders.stream().filter(order -> filterByOrderItemCategory(service.getOrderOrThrowError(order.id()), categoryId)).toList();

        if (orderDate != null) orders = orders.stream().filter(order -> order.orderDate().equals(orderDate)).toList();

        if (lowerBoundaryAmount != null) orders = orders.stream().filter(order -> order.totalAmount() > lowerBoundaryAmount).toList();

        if (higherBoundaryAmount != null) orders = orders.stream().filter(order -> order.totalAmount() < higherBoundaryAmount).toList();

        return ResponseEntity.ok(orders);

    }

    private boolean filterByOrderItemCategory(Order order, Long categoryId) {
        return order.getItems().stream()
                .anyMatch(item -> Objects.equals(item.getProduct().getCategory().getId(), categoryId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDto createOrder(
            @RequestBody OrderCreateDto dto
    ){
        return service.createOrder(dto, true);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam Long id
    ){
        service.deleteOrder(id);
    }

    @DeleteMapping("/all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll(){
        service.deleteAll();
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OrderResponseDto editOrder(
            @RequestParam Long id,
            @RequestBody OrderCreateDto dto
    ){
        return service.editOrder(id,dto);
    }
}
