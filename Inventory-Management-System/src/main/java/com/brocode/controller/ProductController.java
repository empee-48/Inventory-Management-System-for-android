package com.brocode.controller;

import com.brocode.service.ProductService;
import com.brocode.service.dto.ProductCreateDto;
import com.brocode.service.dto.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory/api/products")
public class ProductController {
    private final ProductService service;

    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false) Long id,
            @RequestParam(defaultValue = "") String productName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean lowStock,
            @RequestParam(required = false) Double lowerBoundaryPrice,
            @RequestParam(required = false) Double higherBoundaryPrice
    ){
        if (id != null) return ResponseEntity.ok(service.getProduct(id));

        List<ProductResponseDto> products = service.getAll();

        if (!productName.isEmpty()) products = products.stream().filter(product -> product.name().equalsIgnoreCase(productName)).toList();

        if (categoryId != null) products = products.stream().filter(product -> product.categoryId().equals(categoryId)).toList();

        if (lowStock != null) products = products.stream().filter(product -> product.inStock() < product.warningStockLevel()).toList();

        if (lowerBoundaryPrice != null) products = products.stream().filter(product -> product.price() > lowerBoundaryPrice).toList();

        if (higherBoundaryPrice != null) products = products.stream().filter(product -> product.price() < higherBoundaryPrice).toList();

        return ResponseEntity.ok(products);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDto createProduct(
            @RequestBody ProductCreateDto dto
    ){
        return service.createProduct(dto);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam Long id
    ){
        service.deleteProduct(id);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ProductResponseDto editProduct(
            @RequestBody ProductCreateDto dto,
            @RequestParam Long id
    ){
        return service.editProduct(id,dto);
    }

    @DeleteMapping("/all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll(){
        service.deleteAll();
    }
}
