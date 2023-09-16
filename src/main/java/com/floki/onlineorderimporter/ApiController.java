package com.floki.onlineorderimporter;

import com.floki.onlineorderimporter.model.OrderTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ApiController {

    private final ApiService apiService;

    @Autowired
    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping("/fetchData")
    public String fetchData() {
        return apiService.fetchData();
    }

    @GetMapping("/fetchAndSaveData")
    public List<OrderTable> fetchAndSaveData() {
        return apiService.fetchAndSaveData();
    }

    @GetMapping("/fetchAndSaveDataLoop")
    public void fetchAndSaveDataLoop() {
        apiService.fetchAndSaveDataLoop();
    }

}