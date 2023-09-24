package com.floki.onlineorderimporter;

import com.floki.onlineorderimporter.model.OrderTable;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;


    public Page<OrderTable> getOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return orderRepository.findAll(pageable);
    }

    public List<OrderTable> getFirstXOrders(int x) {
        Pageable pageable = PageRequest.of(0, x, Sort.by("id").ascending());
        return orderRepository.findTopXOrders(pageable);
    }


    public Map<String, Long> getCountByRegion() {
        List<Object[]> results = orderRepository.countByRegion();
        return results.stream().collect(Collectors.toMap(o -> (String) o[0], o -> (Long) o[1]));
    }

    public Map<String, Long> getCountByCountry() {
        List<Object[]> results = orderRepository.countByCountry();
        return results.stream().collect(Collectors.toMap(o -> (String) o[0], o -> (Long) o[1]));
    }

    public Map<String, Long> getCountByItemType() {
        List<Object[]> results = orderRepository.countByItemType();
        return results.stream().collect(Collectors.toMap(o -> (String) o[0], o -> (Long) o[1]));
    }

    public Map<String, Long> getCountBySalesChannel() {
        List<Object[]> results = orderRepository.countBySalesChannel();
        return results.stream().collect(Collectors.toMap(o -> (String) o[0], o -> (Long) o[1]));
    }

    public Map<String, Long> getCountByOrderPriority() {
        List<Object[]> results = orderRepository.countByOrderPriority();
        return results.stream().collect(Collectors.toMap(o -> (String) o[0], o -> (Long) o[1]));
    }

    public void deleteAll() {
        orderRepository.deleteAll();
    }


    public byte[] generateCSVFile() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            // Escribir el encabezado
            csvWriter.writeNext(new String[]{
                    "Order ID",
                    "Order Priority",
                    "Order Date",
                    "Region",
                    "Country",
                    "Item Type",
                    "Sales Channel",
                    "Ship Date",
                    "Units Sold",
                    "Unit Price",
                    "Unit Cost",
                    "Total Revenue",
                    "Total Cost",
                    "Total Profit"
            });

            // Recuperar datos y escribir en el archivo CSV
            List<OrderTable> orders = orderRepository.findAll();
            for (OrderTable order : orders) {
                csvWriter.writeNext(new String[]{
                        String.valueOf(order.getId()),
                        order.getPriority(),
                        order.getFormattedDate(),
                        order.getRegion(),
                        order.getCountry(),
                        order.getItem_type(),
                        order.getSales_channel(),
                        order.getShipFormattedDate(),
                        String.valueOf(order.getUnits_sold()),
                        String.valueOf(order.getUnit_price()),
                        String.valueOf(order.getUnit_cost()),
                        String.valueOf(order.getTotal_revenue()),
                        String.valueOf(order.getTotal_cost()),
                        String.valueOf(order.getTotal_profit())
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }


}
