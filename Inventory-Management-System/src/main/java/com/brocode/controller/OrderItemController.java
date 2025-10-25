package com.brocode.controller;

import com.brocode.service.OrderItemsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory/api/items")
@RequiredArgsConstructor
public class OrderItemController {
    private final OrderItemsService service;
    
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam Long id
    ){
        service.DeleteOrderItem(id);
    }
}
