package com.brocode.service;

import com.brocode.entity.*;
import com.brocode.repo.ActivityLogRepo;
import com.brocode.repo.BatchRepo;
import com.brocode.repo.ProductRepo;
import com.brocode.repo.SaleItemRepo;
import com.brocode.service.dto.SaleItemCreateDto;
import com.brocode.service.dto.SaleItemResponseDto;
import com.brocode.utils.Activity;
import com.brocode.utils.ProductOutOfStockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SaleItemsService {
    private final SaleItemMapper mapper;
    private final SaleItemRepo repo;
    private final ActivityLogRepo logRepo;
    private final ProductRepo productRepo;
    private final BatchRepo batchRepo;

    public List<SaleItemResponseDto> getSaleItems(){
        return repo.findAll().stream().map(mapper::saleItemToResponse).toList();
    }

    @Transactional
    public SaleItemResponseDto createSaleItems(SaleItemCreateDto dto, Sale sale) {
        SaleItem item = repo.save(mapper.createToSaleItem(dto, sale));
        Product product = item.getProduct();
        Batch batch = item.getBatch();

        double newProductInstock = product.getInStock() - item.getAmount();

        if (newProductInstock < 0)
            throw new ProductOutOfStockException(
                    product.getName(),
                    item.getAmount(),
                    product.getInStock()
            );

        product.setInStock(newProductInstock);
        batch.setStockLeft(batch.getStockLeft() - item.getAmount());

        batchRepo.save(batch);
        productRepo.save(product);

        createLog(item, Activity.CREATE);
        return mapper.saleItemToResponse(item);
    }

    @Transactional
    public void delete(Long id){
        SaleItem item = repo.findById(id).orElseThrow(() -> new NoSuchElementException("SaleItem Not Found"));
        Product product = item.getProduct();
        Batch batch = item.getBatch();

        product.setInStock(product.getInStock() + item.getAmount());
        batch.setStockLeft(batch.getStockLeft() + item.getAmount());

        productRepo.save(product);
        batchRepo.save(batch);

        repo.delete(item);

        createLog(item, Activity.DELETE);
    }

    private void createLog(SaleItem item, Activity activity){
        ActivityLog activityLog = ActivityLog.builder()
                .activity(activity)
                .description(String.format("SaleItem ID %d Price %s Name %s",
                        item.getId(),
                        item.getSalePrice(),
                        item.getProduct().getName())
                )
                .build();

        logRepo.save(activityLog);
    }

}
