package com.brocode.controller;

import com.brocode.service.SaleItemsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory/api/sales/items")
@RequiredArgsConstructor
public class SaleItemController {
    private final SaleItemsService service;

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam Long id
    ){
        service.delete(id);
    }
}
