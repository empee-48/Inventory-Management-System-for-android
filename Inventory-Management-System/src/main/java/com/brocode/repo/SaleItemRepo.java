package com.brocode.repo;

import com.brocode.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleItemRepo extends JpaRepository<SaleItem, Long> {
}
