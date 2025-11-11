package com.brocode.controller;

import com.brocode.service.SupplierService;
import com.brocode.service.dto.SupplierCreateDto;
import com.brocode.service.dto.SupplierResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory/api/suppliers")
public class SupplierController {
    private final SupplierService service;

    @GetMapping
    public ResponseEntity<?> getSupplier(
            @RequestParam(required = false) Long id
    ){
        if (id != null) return ResponseEntity.ok(service.getSupplier(id));

        return ResponseEntity.ok(service.getAll().reversed());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierResponseDto createSupplier(
            @RequestBody SupplierCreateDto dto
    ){
        return service.createSupplier(dto);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SupplierResponseDto editSupplier(
            @RequestParam Long id,
            @RequestBody SupplierCreateDto dto
    ){
        return service.editSupplier(id, dto);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam Long id
    ){
        service.delete(id);
    }

    @DeleteMapping("/all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll(){
        service.deleteAll();
    }
}
