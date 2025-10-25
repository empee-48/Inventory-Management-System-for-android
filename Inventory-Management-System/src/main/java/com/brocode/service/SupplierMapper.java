package com.brocode.service;

import com.brocode.entity.Supplier;
import com.brocode.service.dto.SupplierCreateDto;
import com.brocode.service.dto.SupplierResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SupplierMapper {
    private final OrderMapper mapper;

    public SupplierResponseDto supplierToResponse(Supplier supplier){
        return new SupplierResponseDto(
                supplier.getId(),
                supplier.getName(),
                supplier.getContact(),
                supplier.getAddress(),
                supplier.getContactPerson(),
                supplier.getOrders().stream().map(mapper::orderToResponse).toList(),
                supplier.getCreatedAt(),
                supplier.getLastModifiedAt(),
                supplier.getCreatedBy(),
                supplier.getLastModifiedBy()
        );
    }

    public Supplier createToSupplier(SupplierCreateDto dto){
        return Supplier.builder()
                .contact(dto.contact())
                .address(dto.address())
                .name(dto.name())
                .contactPerson(dto.contactName())
                .build();
    }

}