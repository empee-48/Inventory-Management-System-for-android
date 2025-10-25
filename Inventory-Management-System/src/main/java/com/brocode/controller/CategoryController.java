package com.brocode.controller;

import com.brocode.service.CategoryService;
import com.brocode.service.dto.CategoryCreateDto;
import com.brocode.service.dto.CategoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;

    @GetMapping
    public ResponseEntity<?> getCategory(
            @RequestParam(required = false) Long id,
            @RequestParam(defaultValue = "") String categoryName
    ){
        if (id != null) return ResponseEntity.ok(service.getCategory(id));

        if (!categoryName.isEmpty()) return ResponseEntity.ok(service.getAll().stream().filter(category -> category.name().equalsIgnoreCase(categoryName)));

        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDto createCategory(
            @RequestBody CategoryCreateDto dto
    ){
        return service.createCategory(dto);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CategoryResponseDto editCategory(
           @RequestParam Long id,
           @RequestBody CategoryCreateDto dto
    ){
        return service.editCategory(id, dto);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam Long id
    ){
        service.deleteCategory(id);
    }

    @DeleteMapping("/all")
    public void deleteAll(){
        service.deleteAll();
    }
}
