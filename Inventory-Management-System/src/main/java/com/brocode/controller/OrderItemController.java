package com.brocode.controller;

import com.brocode.service.OrderItemsService;
import com.brocode.service.dto.OrderItemResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/inventory/api/items")
@RequiredArgsConstructor
public class OrderItemController {
    private final OrderItemsService service;

    @GetMapping
    public ResponseEntity<?> getOrders(
            @RequestParam(required = false) Long id
    ){
        List<OrderItemResponseDto> orders = service.getOrders().reversed();

        if (id != null) return ResponseEntity.ok(orders.stream().filter(order -> Objects.equals(order.id(), id)).toList());

        return ResponseEntity.ok(orders);
    }
    
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam Long id
    ){
        service.delete(id);
    }
}
