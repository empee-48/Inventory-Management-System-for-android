package com.brocode.service;

import com.brocode.entity.ActivityLog;
import com.brocode.entity.Batch;
import com.brocode.entity.Order;
import com.brocode.repo.ActivityLogRepo;
import com.brocode.repo.OrderRepo;
import com.brocode.service.dto.OrderCreateDto;
import com.brocode.service.dto.OrderItemCreateDto;
import com.brocode.service.dto.OrderResponseDto;
import com.brocode.utils.Activity;
import com.brocode.utils.IdGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MyOrderService {
    private final OrderMapper mapper;
    private final OrderRepo repo;
    private final ActivityLogRepo logRepo;
    private final OrderItemsService orderItemsService;
    private final BatchService batchService;

    public Order getOrderOrThrowError(Long id){
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException(String.format("Order ID %d Not Found", id)));
    }

    private void createOrderItems(List<OrderItemCreateDto> items, Order order, boolean addStock){
        items.forEach(item -> orderItemsService.createOrderItems(item, order, addStock));
    }

    private void createOrderBatches(List<OrderItemCreateDto> items, Order order){
        items.forEach(item -> {
            batchService.createBatch(item, order);
        });
    }

    public List<OrderResponseDto> getAll(){
        return repo.findAll().stream().map(mapper::orderToResponse).toList();
    }

    public OrderResponseDto getOrder(Long id){
        return mapper.orderToResponse(getOrderOrThrowError(id));
    }

    /**
     *
     * @param dto for creating Order
     * @param addStock confirms whether we add the number of items to the Product.inStock property
     * @return
     */
    @Transactional
    public OrderResponseDto createOrder(OrderCreateDto dto, boolean addStock){
        Order order = repo.save(mapper.createToOrder(dto));
        order.setOrderId(IdGenerator.generateOrderId(order));

        if (dto.items() != null) {
            createOrderItems(dto.items(), order, addStock);
            createOrderBatches(dto.items(), order);
        };

        createLog(order, Activity.CREATE);
        return mapper.orderToResponse(repo.save(order));
    }

    @Transactional
    public void deleteOrder(Long id){
        Order order = getOrderOrThrowError(id);

        if (order != null) order.getItems().forEach(orderItem -> orderItemsService.delete(orderItem.getId()));

        createLog(order, Activity.DELETE);
        repo.delete(order);
    }

    @Transactional
    public OrderResponseDto editOrder(Long id, OrderCreateDto dto){
        Order order = getOrderOrThrowError(id);

        order.setOrderDate(dto.orderDate());
        order.setSupplier(mapper.createToOrder(dto).getSupplier());
        order.setTotalAmount(dto.totalAmount());

        final boolean ADD_STOCK = true;

        if (dto.items() != null) createOrderItems(dto.items(), order, ADD_STOCK);

        createLog(order, Activity.MODIFY);
        return mapper.orderToResponse(repo.save(order));
    }

    private void createLog(Order order, Activity activity){
        ActivityLog activityLog = ActivityLog.builder()
                .activity(activity)
                .description(String.format("Order ID %d Date %s",
                        order.getId(),
                        order.getOrderDate())
                )
                .build();

        logRepo.save(activityLog);
    }

    public void deleteAll() {
        repo.deleteAll();
    }
}
