package com.floki.onlineorderimporter;

import com.floki.onlineorderimporter.model.OrderTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/firstX/{x}")
    public ResponseEntity<List<OrderTable>> getFirstXOrders(@PathVariable int x) {
        List<OrderTable> orders = orderService.getFirstXOrders(x);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<OrderTable>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<OrderTable> orders = orderService.getOrders(page, size);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/deleteAll")
    public void deleteAll() {
        orderService.deleteAll();
    }

    @GetMapping("/countByRegion")
    public Map<String, Long> getCountByRegion() {
        return orderService.getCountByRegion();
    }

    @GetMapping("/countByCountry")
    public Map<String, Long> getCountByCountry() {
        return orderService.getCountByCountry();
    }

    @GetMapping("/countByPriority")
    public Map<String, Long> getCountByOrderPriority() {
        return orderService.getCountByOrderPriority();
    }

    @GetMapping("/countByItemType")
    public Map<String, Long> getCountByItemType() {
        return orderService.getCountByItemType();
    }

    @GetMapping("/countBySalesChannel")
    public Map<String, Long> getCountBySalesChannel() {
        return orderService.getCountBySalesChannel();
    }

    @GetMapping("/generate-csv")
    public ResponseEntity<byte[]> downloadCSV() throws IOException {
        byte[] csvData = orderService.generateCSVFile();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename("orders.csv").build());
        return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
    }


}
