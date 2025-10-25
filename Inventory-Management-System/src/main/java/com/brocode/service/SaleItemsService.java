package com.brocode.service;

import com.brocode.entity.ActivityLog;
import com.brocode.entity.Sale;
import com.brocode.entity.SaleItem;
import com.brocode.repo.ActivityLogRepo;
import com.brocode.repo.SaleItemRepo;
import com.brocode.service.dto.SaleItemCreateDto;
import com.brocode.service.dto.SaleItemResponseDto;
import com.brocode.utils.Activity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SaleItemsService {
    private final SaleItemMapper mapper;
    private final SaleItemRepo repo;
    private final ActivityLogRepo logRepo;

    @Transactional
    public SaleItemResponseDto createOrderItems(SaleItemCreateDto dto, Sale sale) {
        SaleItem item = mapper.createToSaleItem(dto, sale);
        createLog(item, Activity.CREATE);
        return mapper.saleItemToResponse(item);
    }

    @Transactional
    public void delete(Long id){
        SaleItem item = repo.findById(id).orElseThrow(() -> new NoSuchElementException("SaleItem Not Found"));
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
