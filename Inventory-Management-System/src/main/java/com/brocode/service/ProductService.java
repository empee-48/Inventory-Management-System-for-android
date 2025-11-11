package com.brocode.service;

import com.brocode.entity.ActivityLog;
import com.brocode.entity.Order;
import com.brocode.entity.Product;
import com.brocode.repo.ActivityLogRepo;
import com.brocode.repo.OrderItemRepo;
import com.brocode.repo.ProductRepo;
import com.brocode.service.dto.*;
import com.brocode.utils.Activity;
import com.brocode.utils.IdGenerator;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductMapper mapper;
    private final ProductRepo repo;
    private final ActivityLogRepo logRepo;
    private final MyOrderService orderService;
    private final OrderItemsService orderItemsService;
    private final OrderItemRepo orderItemRepo;

    public Product getProductOrThrowError(Long id){
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Category Not Found"));
    }

    private void createProductOrder(Product product) {
        final Long supplierId = 1L;

        OrderCreateDto dto = new OrderCreateDto(
                supplierId,
                product.getCreatedAt().toLocalDate(),
                new ArrayList<>(Collections.singleton(new OrderItemCreateDto(
                        product.getId(),
                        product.getInStock(),
                        product.getPrice()
                ))),
                product.getInStock()*product.getPrice()
        );

        orderService.createOrder(dto, false);
    }

    public List<ProductResponseDto> getAll(){
        return repo.findAll().stream().map(mapper::productToResponse).toList();
    }

    public ProductResponseDto getProduct(Long id){
        return mapper.productToResponse(getProductOrThrowError(id));
    }

    @Transactional
    public ProductResponseDto createProduct(ProductCreateDto dto){
        Product product = repo.save(mapper.createToProduct(dto));
        product.setProductKey(IdGenerator.generateProductKey(product));

        if (product.getInStock() > 0) createProductOrder(product);

        createLog(product, Activity.CREATE);
        return mapper.productToResponse(repo.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        deleteAvailableOrderItems(id);
        Product product = getProductOrThrowError(id);
        repo.deleteById(id);
        createLog(product, Activity.DELETE);
    }

    public void deleteAvailableOrderItems(Long productId){
        orderItemRepo.findAll()
                .stream()
                .filter(item -> Objects.equals(item.getProduct().getId(), productId))
                .forEach(item -> {
                    Order order = orderService.getOrderOrThrowError(item.getOrder().getId());

//                    orderItemsService.delete(item.getId());
                    if (order.getItems().size() == 1) orderService.deleteOrder(order.getId());

                });
    }

    @Transactional
    public ProductResponseDto editProduct(Long id, ProductCreateDto dto){
        Product product = getProductOrThrowError(id);

        product.setName(dto.name());
        product.setInStock(dto.inStock());
        product.setPrice(dto.price());
        product.setDescription(dto.description());
        product.setWarningStockLevel(dto.warningStockLevel());
        product.setUnit(dto.unit());
        product.setCategory(mapper.createToProduct(dto).getCategory());

        createLog(product, Activity.MODIFY);
        return mapper.productToResponse(repo.save(product));
    }

    private void createLog(Product product, Activity activity){
        ActivityLog activityLog = ActivityLog.builder()
                .activity(activity)
                .description(String.format("Product ID %d Name %s",
                        product.getId(),
                        product.getName()
                ))
                .build();

        logRepo.save(activityLog);
    }

    public void deleteAll() {
        repo.deleteAll();
    }
}
