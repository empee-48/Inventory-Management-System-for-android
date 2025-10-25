package com.brocode.service;

import com.brocode.entity.ActivityLog;
import com.brocode.entity.Order;
import com.brocode.entity.OrderItem;
import com.brocode.repo.ActivityLogRepo;
import com.brocode.repo.OrderItemRepo;
import com.brocode.service.dto.OrderItemCreateDto;
import com.brocode.service.dto.OrderItemResponseDto;
import com.brocode.utils.Activity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderItemsService {
    private final OrderItemRepo repo;
    private final OrderItemMapper mapper;
    private final ActivityLogRepo logRepo;

    public OrderItem getOrderItemOrThrowError(Long id){
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Order Not Found"));
    }

    public OrderItemResponseDto createOrderItems(OrderItemCreateDto dto, Order order) {
        OrderItem item = repo.save(mapper.createToOrderItem(dto, order));
        createLog(item, Activity.CREATE);
        return mapper.orderItemToResponse(item);
    }

    public void DeleteOrderItem(Long id){
        OrderItem orderItem = getOrderItemOrThrowError(id);
        repo.delete(orderItem);
        createLog(orderItem, Activity.DELETE);
    }

    private void createLog(OrderItem orderItem, Activity activity){
        ActivityLog activityLog = ActivityLog.builder()
                .activity(activity)
                .description(String.format("OrderItem ID %d  Price %s Product Name %s",
                        orderItem.getId(),
                        orderItem.getOrderPrice(),
                        orderItem.getProduct().getName())
                )
                .build();

        logRepo.save(activityLog);
    }
}
