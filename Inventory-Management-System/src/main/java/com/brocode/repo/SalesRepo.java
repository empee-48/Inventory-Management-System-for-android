package com.brocode.repo;

import com.brocode.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesRepo extends JpaRepository<Sale, Long> {
}
