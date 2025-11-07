package com.brocode.controller;

import com.brocode.service.SaleItemsService;
import com.brocode.service.dto.SaleItemResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/inventory/api/sales/items")
@RequiredArgsConstructor
public class SaleItemController {
    private final SaleItemsService service;

    @GetMapping
    public ResponseEntity<?> getSaleItems(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long batchId
    ){
        List<SaleItemResponseDto> sales = service.getSaleItems();

        if (id != null) return ResponseEntity.ok(sales.stream().filter(sale -> Objects.equals(sale.id(), id)).toList());

        if (batchId != null) return ResponseEntity.ok(sales.stream().filter(sale -> Objects.equals(sale.batchId(), batchId)).toList());

        return ResponseEntity.ok(sales);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam Long id
    ){
        service.delete(id);
    }
}
