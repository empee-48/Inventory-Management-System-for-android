package com.brocode.service;

import com.brocode.entity.ActivityLog;
import com.brocode.entity.Order;
import com.brocode.entity.OrderItem;
import com.brocode.entity.Product;
import com.brocode.repo.ActivityLogRepo;
import com.brocode.repo.OrderItemRepo;
import com.brocode.repo.ProductRepo;
import com.brocode.service.dto.OrderItemCreateDto;
import com.brocode.service.dto.OrderItemResponseDto;
import com.brocode.utils.Activity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderItemsService {
    private final OrderItemRepo repo;
    private final OrderItemMapper mapper;
    private final ActivityLogRepo logRepo;
    private final ProductRepo productRepo;

    public OrderItem getOrderItemOrThrowError(Long id){
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Order Not Found"));
    }

    public List<OrderItemResponseDto> getOrders(){
        return repo.findAll().stream().map(mapper::orderItemToResponse).toList();
    }

    @Transactional
    public OrderItemResponseDto createOrderItems(OrderItemCreateDto dto, Order order, boolean addStock) {
        OrderItem item = repo.save(mapper.createToOrderItem(dto, order));

        if (addStock){
            Product product = item.getProduct();
            product.setInStock(product.getInStock() + item.getAmount());

            productRepo.save(product);
        }

        createLog(item, Activity.CREATE);
        return mapper.orderItemToResponse(item);
    }

    @Transactional
    public void delete(Long id){
        OrderItem orderItem = getOrderItemOrThrowError(id);

        Product product = orderItem.getProduct();
        product.setInStock(product.getInStock() - orderItem.getAmount());

        productRepo.save(product);

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
