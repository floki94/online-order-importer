package com.floki.onlineorderimporter;

import com.floki.onlineorderimporter.model.OrderTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderTable, UUID> {
}