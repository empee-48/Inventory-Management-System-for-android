package com.brocode.service;

import com.brocode.entity.ActivityLog;
import com.brocode.entity.Supplier;
import com.brocode.repo.ActivityLogRepo;
import com.brocode.repo.SupplierRepo;
import com.brocode.service.dto.SupplierCreateDto;
import com.brocode.service.dto.SupplierResponseDto;
import com.brocode.utils.Activity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierMapper mapper;
    private final SupplierRepo repo;
    private final ActivityLogRepo logRepo;

    private Supplier getSupplierOrThrowError(Long id){
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Category Not Found"));
    }

    public List<SupplierResponseDto> getAll(){
        return repo.findAll().stream().map(mapper::supplierToResponse).toList();
    }

    public SupplierResponseDto getSupplier(Long id){
        return mapper.supplierToResponse(getSupplierOrThrowError(id));
    }

    @Transactional
    public SupplierResponseDto createSupplier(SupplierCreateDto dto){
        Supplier supplier = repo.save(mapper.createToSupplier(dto));
        createLog(supplier, Activity.CREATE);
        return mapper.supplierToResponse(supplier);
    }

    @Transactional
    public void delete(Long id){
        Supplier supplier = getSupplierOrThrowError(id);
        createLog(supplier, Activity.DELETE);
    }

    @Transactional
    public SupplierResponseDto editSupplier(Long id, SupplierCreateDto dto){
        Supplier supplier = getSupplierOrThrowError(id);

        supplier.setAddress(dto.address());
        supplier.setContact(dto.contact());
        supplier.setName(dto.name());
        supplier.setContactPerson(dto.contactName());

        createLog(supplier, Activity.MODIFY);
        return mapper.supplierToResponse(repo.save(supplier));
    }

    private void createLog(Supplier supplier, Activity activity){
        ActivityLog activityLog = ActivityLog.builder()
                .activity(activity)
                .description(String.format("Supplier ID %d Name %s",
                        supplier.getId(),
                        supplier.getName()
                ))
                .build();

        logRepo.save(activityLog);
    }

    public void deleteAll() {
        repo.deleteAll();
    }
}
