package com.brocode.service;

import com.brocode.entity.ActivityLog;
import com.brocode.entity.Product;
import com.brocode.repo.ActivityLogRepo;
import com.brocode.repo.ProductRepo;
import com.brocode.service.dto.ProductCreateDto;
import com.brocode.service.dto.ProductResponseDto;
import com.brocode.utils.Activity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductMapper mapper;
    private final ProductRepo repo;
    private final ActivityLogRepo logRepo;

    public Product getProductOrThrowError(Long id){
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Category Not Found"));
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
        createLog(product, Activity.CREATE);
        return mapper.productToResponse(product);
    }

    @Transactional
    public void deleteProduct(Long id){
        Product product = getProductOrThrowError(id);
        repo.delete(product);
        createLog(product, Activity.DELETE);
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
