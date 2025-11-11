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
import java.util.Objects;

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

    public Batch getNextAvailableBatch(SaleItemCreateDto dto){
        List<Batch> batches = batchRepo.findAll()
                .stream()
                .filter(batch1 -> Objects.equals(batch1.getProduct().getId(), dto.productId()))
                .filter(batch1 -> batch1.getStockLeft() > 0)
                .toList();

        if (batches.isEmpty()) throw new NoSuchElementException("Product Out Of Stock");

        return batches.getFirst();
    }

    public void processProduct(SaleItemCreateDto dto, Product product){
        double newProductInstock = product.getInStock() - dto.amount();

        if (newProductInstock < 0)
            throw new ProductOutOfStockException(
                    product.getName(),
                    dto.amount(),
                    product.getInStock()
            );

        product.setInStock(newProductInstock);

        productRepo.save(product);
    }

    @Transactional
    public SaleItemResponseDto createSaleItems(SaleItemCreateDto dto, Sale sale) {
        SaleItem item = mapper.createToSaleItem(dto, sale);
        Batch batch = item.getBatch();

        Double saleAmount = dto.amount();


        while(saleAmount > 0){

            if (batch.getStockLeft() < 1) batch = getNextAvailableBatch(dto);
            Double batchAmount = batch.getStockLeft();

            double amountSold = 0;
            SaleItem saveItem = SaleItem
                    .builder()
                    .product(item.getProduct())
                    .salePrice(dto.price())
                    .batch(batch)
                    .sale(item.getSale())
                    .build();

            while(batchAmount > 0 && saleAmount > 0){
                amountSold++;
                batchAmount--;
                saleAmount--;
            }

            saveItem.setAmount(amountSold);
            batch.setStockLeft(batchAmount);

            repo.save(saveItem);
            batchRepo.save(batch);
        }

        Product product = item.getProduct();

        processProduct(dto, product);

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
