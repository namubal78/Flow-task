package com.flowtask.domain.repository;

import com.flowtask.domain.entity.BlockedSystem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockedSystemRepository extends JpaRepository<BlockedSystem, Long> {
}
