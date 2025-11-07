package com.brocode.service;

import com.brocode.entity.Batch;
import com.brocode.entity.Order;
import com.brocode.repo.BatchRepo;
import com.brocode.service.dto.BatchResponseDto;
import com.brocode.service.dto.OrderItemCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchService {
    private final BatchMapper mapper;
    private final BatchRepo repo;

    public List<BatchResponseDto> getBatches(){
        return repo.findAll().stream().map(mapper::batchToResponse).toList();
    }

    public void createBatch(OrderItemCreateDto item, Order order){
        repo.save(mapper.createToBatch(item, order));
    }

    public void delete(Long id){
        repo.deleteById(id);
    }

    public void deleteAll(){
        repo.deleteAll();
    }
}
