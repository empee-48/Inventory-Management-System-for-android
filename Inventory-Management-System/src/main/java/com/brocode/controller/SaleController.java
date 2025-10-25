package com.brocode.controller;

import com.brocode.entity.Sale;
import com.brocode.service.SaleService;
import com.brocode.service.dto.SaleCreateDto;
import com.brocode.service.dto.SaleResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/inventory/api/sales")
@RequiredArgsConstructor
public class SaleController {
    private final SaleService service;

    @GetMapping
    public ResponseEntity<?> getSales(
            @RequestParam(required = false) Long id,
            @RequestParam(defaultValue = "") Long categoryId,
            @RequestParam(required = false) LocalDate saleDateStart,
            @RequestParam(required = false) LocalDate saleDateEnd,
            @RequestParam(required = false) Double lowerSalesBoundary,
            @RequestParam(required = false) Double higherSalesBoundary
    ){
        if (id != null) return ResponseEntity.ok(service.getSale(id));

        List<SaleResponseDto> sales = service.getAll();

        if (categoryId != null) sales = sales.stream().filter(sale -> filterBySaleItemCategory(service.getSaleOrThrowError(sale.id()), categoryId)).toList();

        if (saleDateEnd != null && saleDateStart != null) sales = sales.stream().filter(sale -> filterByDates(sale, saleDateStart, saleDateEnd)).toList();

        if (lowerSalesBoundary != null) sales = sales.stream().filter(sale -> sale.totalAmount() > lowerSalesBoundary).toList();

        if (higherSalesBoundary != null) sales = sales.stream().filter(sale -> sale.totalAmount() < higherSalesBoundary).toList();

        return ResponseEntity.ok(sales);
    }

    private boolean filterBySaleItemCategory(Sale sale, Long categoryId) {
        return sale.getItems().stream()
                .anyMatch(item -> Objects.equals(item.getProduct().getCategory().getId(), categoryId));
    }

    private boolean filterByDates(SaleResponseDto sale, LocalDate lowerDate, LocalDate higherDate){
        boolean IS_AFTER_LOWER_DATE = sale.saleDate().isAfter(lowerDate);
        boolean IS_AFTER_HIGHER_DATE = sale.saleDate().isBefore(higherDate);

        return IS_AFTER_HIGHER_DATE && IS_AFTER_LOWER_DATE;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponseDto createSale(
            @RequestBody SaleCreateDto dto
    ){
        return service.createSale(dto);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSale(
            @RequestParam Long id
    ){
        service.delete(id);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SaleResponseDto editSale(
            @RequestParam Long id,
            @RequestBody SaleCreateDto dto
    ){
        return service.editSale(id, dto);
    }

    @DeleteMapping("/all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll(){
        service.deleteAll();
    }
}
