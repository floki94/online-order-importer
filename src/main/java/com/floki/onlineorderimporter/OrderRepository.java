package com.floki.onlineorderimporter;

import com.floki.onlineorderimporter.model.OrderTable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderTable, UUID> {
    @Query(value = "SELECT o FROM OrderTable o ORDER BY o.id ASC")
    List<OrderTable> findTopXOrders(Pageable pageable);

    @Query("SELECT o.region, COUNT(o) FROM OrderTable o GROUP BY o.region")
    List<Object[]> countByRegion();

    @Query("SELECT o.country, COUNT(o) FROM OrderTable o GROUP BY o.country")
    List<Object[]> countByCountry();

    @Query("SELECT o.item_type, COUNT(o) FROM OrderTable o GROUP BY o.item_type")
    List<Object[]> countByItemType();

    @Query("SELECT o.sales_channel, COUNT(o) FROM OrderTable o GROUP BY o.sales_channel")
    List<Object[]> countBySalesChannel();

    @Query("SELECT o.priority, COUNT(o) FROM OrderTable o GROUP BY o.priority")
    List<Object[]> countByOrderPriority();



}