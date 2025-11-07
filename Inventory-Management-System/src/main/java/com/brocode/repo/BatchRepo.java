package com.brocode.repo;

import com.brocode.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchRepo extends JpaRepository<Batch, Long> {
}
