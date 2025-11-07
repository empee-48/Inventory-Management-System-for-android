package com.brocode.controller;

import com.brocode.service.BatchService;
import com.brocode.service.dto.BatchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory/api/batch")
public class BatchController {
    private final BatchService service;

    @GetMapping
    public List<BatchResponseDto> getBatch(){
        return service.getBatches();
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
