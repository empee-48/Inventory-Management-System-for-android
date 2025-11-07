package com.brocode.service;

import com.brocode.entity.ActivityLog;
import com.brocode.entity.Product;
import com.brocode.entity.Sale;
import com.brocode.repo.ActivityLogRepo;
import com.brocode.repo.ProductRepo;
import com.brocode.repo.SalesRepo;
import com.brocode.service.dto.SaleCreateDto;
import com.brocode.service.dto.SaleItemCreateDto;
import com.brocode.service.dto.SaleResponseDto;
import com.brocode.utils.Activity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SaleService {
    private final SaleMapper mapper;
    private final SalesRepo repo;
    private final ActivityLogRepo logRepo;
    private final SaleItemsService saleItemsService;
    private final ProductRepo productRepo;

    public Sale getSaleOrThrowError(Long id){
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Category Not Found"));
    }

    private void createSaleItems(List<SaleItemCreateDto> items, Sale sale){
        items.forEach(item -> saleItemsService.createSaleItems(item, sale));
    }

    public List<SaleResponseDto> getAll(){
        return repo.findAll().stream().map(mapper::saleToResponse).toList();
    }

    public SaleResponseDto getSale(Long id){
        return mapper.saleToResponse(getSaleOrThrowError(id));
    }

    @Transactional
    public SaleResponseDto createSale(SaleCreateDto dto){
        Sale sale = repo.save(mapper.createToSale(dto));

        if (dto.items() != null) createSaleItems(dto.items(), sale);

        createLog(sale, Activity.CREATE);
        return mapper.saleToResponse(sale);
    }

    @Transactional
    public void delete(Long id){
        Sale sale = getSaleOrThrowError(id);
        repo.delete(sale);
        createLog(sale, Activity.DELETE);
    }
    @Transactional
    public SaleResponseDto editSale(Long id, SaleCreateDto dto){
        Sale sale = getSaleOrThrowError(id);

        sale.setSaleDate(dto.saleDate());
        sale.setTotalAmount(dto.totalAmount());

        if (dto.items() != null) createSaleItems(dto.items(), sale);

        createLog(sale, Activity.MODIFY);
        return mapper.saleToResponse(repo.save(sale));
    }

    private void createLog(Sale sale, Activity activity){
        ActivityLog activityLog = ActivityLog.builder()
                .activity(activity)
                .description(String.format("Sale ID %d Date %s",
                        sale.getId(),
                        sale.getSaleDate()
                ))
                .build();

        logRepo.save(activityLog);
    }

    public void deleteAll() {
        repo.deleteAll();
    }
}
