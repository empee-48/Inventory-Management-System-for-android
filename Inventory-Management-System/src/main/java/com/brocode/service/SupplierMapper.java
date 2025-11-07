package com.brocode.service;

import com.brocode.entity.Supplier;
import com.brocode.service.dto.OrderResponseDto;
import com.brocode.service.dto.SupplierCreateDto;
import com.brocode.service.dto.SupplierResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierMapper {
    private final OrderMapper mapper;

    public SupplierResponseDto supplierToResponse(Supplier supplier){
        List<OrderResponseDto> orders = null;

        if (supplier.getOrders() != null) orders = supplier.getOrders().stream().map(mapper::orderToResponse).toList();

        return new SupplierResponseDto(
                supplier.getId(),
                supplier.getName(),
                supplier.getContact(),
                supplier.getAddress(),
                supplier.getContactPerson(),
                orders,
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