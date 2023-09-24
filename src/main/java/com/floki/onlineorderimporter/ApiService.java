package com.floki.onlineorderimporter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.floki.onlineorderimporter.model.OrderTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ApiService {
    private final WebClient webClient = WebClient.create("https://kata-espublicotech.g3stiona.com/v1/");

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "batchNumber",
            "batchProcessingTime",
            "totalProcessingTime"
    })
    public class BatchProcessingResult implements Serializable {

        @JsonProperty("batchNumber")
        private Long batchNumber;

        @JsonProperty("batchProcessingTime")
        private Long batchProcessingTime;

        @JsonProperty("totalProcessingTime")
        private Long totalProcessingTime;

        public BatchProcessingResult(long batchNumber, long batchProcessingTime, long totalProcessingTime) {
            this.batchNumber = batchNumber;
            this.batchProcessingTime = batchProcessingTime;
            this.totalProcessingTime = totalProcessingTime;
        }

    }

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SimpMessagingTemplate template;

    public String fetchData() {
        String uri = UriComponentsBuilder.fromPath("/orders")
                .queryParam("page", 1)
                .queryParam("max-per-page", 5)
                .toUriString();
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();

    }

    public List<OrderTable> fetchAndSaveData() {
        String uri = UriComponentsBuilder.fromPath("/orders")
                .queryParam("page", 1)
                .queryParam("max-per-page", 5)
                .toUriString();

        String responseJson = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        List<OrderTable> savedOrders = new ArrayList<>();

        try {
            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode contentNode = rootNode.get("content");

            if (contentNode.isArray()) {
                for (JsonNode node : contentNode) {
                    OrderTable order = objectMapper.treeToValue(node, OrderTable.class);
                    OrderTable savedOrder = orderRepository.save(order);
                    savedOrders.add(savedOrder);
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return savedOrders;
    }


    public List<OrderTable> fetchAndSaveDataLoopOld() {
        try {

            String nextUrl = "https://kata-espublicotech.g3stiona.com/v1/orders?page=1&max-per-page=500";

            List<OrderTable> savedOrders = new ArrayList<>();

            while (nextUrl != null) {
                String responseJson = webClient.get()
                        .uri(nextUrl)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());

                JsonNode rootNode = objectMapper.readTree(responseJson);
                JsonNode contentNode = rootNode.get("content");

                if (contentNode.isArray()) {
                    for (JsonNode node : contentNode) {
                        OrderTable order = objectMapper.treeToValue(node, OrderTable.class);
                        OrderTable savedOrder = orderRepository.save(order);
                        savedOrders.add(savedOrder);
                    }
                }
                JsonNode links = rootNode.get("links");
                if (links.has("next")) {
                    nextUrl = String.valueOf(links.get("next"));
                    if (nextUrl.startsWith("\"") && nextUrl.endsWith("\"")) {
                        nextUrl = nextUrl.substring(1, nextUrl.length() - 1);
                    }
                } else {
                    nextUrl = null;

                }
            }
            return savedOrders;

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public void fetchAndSaveDataLoopWebsocket() {
        long startTime = System.currentTimeMillis();
        AtomicLong batchNumber = new AtomicLong(0);
        int batchSize = 10_000;

        Flux.fromIterable(generateUrls())
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(this::fetchDataWithRetry)
                .sequential()
                .buffer(batchSize)
                .flatMap(batch -> {
                    long batchStartTime = System.currentTimeMillis();

                    return Mono.fromRunnable(() -> orderRepository.saveAll(batch))
                            .doOnSuccess(v -> {
                                long batchEndTime = System.currentTimeMillis();
                                long batchDuration = batchEndTime - batchStartTime;
                                long totalDuration = batchEndTime - startTime;

                                BatchProcessingResult result = new BatchProcessingResult(batchNumber.incrementAndGet(), batchDuration, totalDuration);
                                template.convertAndSend("/topic/batchProcessing", result);
                            })
                            .doOnError(error -> logFailedBatch(batch));
                })
                .onErrorContinue((throwable, o) -> {

                })
                .blockLast();
    }

    private Flux<OrderTable> fetchDataFromUrl(String url) {
        String responseJson = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(responseJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        JsonNode contentNode = rootNode.get("content");
        List<OrderTable> orders = new ArrayList<>();
        if (contentNode.isArray()) {
            for (JsonNode node : contentNode) {
                try {
                    OrderTable order = objectMapper.treeToValue(node, OrderTable.class);
                    orders.add(order);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
        return Flux.fromIterable(orders);
    }

    private Flux<OrderTable> fetchDataWithRetry(String url) {
        return fetchDataFromUrl(url)
                .retry(3)
                .onErrorResume(e -> {
                    return Mono.empty();
                });
    }


    public void fetchAndSaveDataLoop() {
        try {
            List<String> urlsToFetch = generateUrls();

            int batchSize = 10_000;

            Flux.fromIterable(urlsToFetch)
                    .parallel()
                    .runOn(Schedulers.boundedElastic())
                    .flatMap(this::fetchDataFromUrl)
                    .sequential()
                    .buffer(batchSize)
                    .flatMap(batch -> Mono.fromRunnable(() -> orderRepository.saveAll(batch))
                            .doOnError(error -> {
                                logFailedBatch(batch);
                            }))
                    .blockLast();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private List<String> generateUrls() {
        List<String> urls = new ArrayList<>();
        for (int i = 1; i <= 2000; i++) {
            urls.add("https://kata-espublicotech.g3stiona.com/v1/orders?page=" + i + "&max-per-page=500");
        }
        return urls;
    }


    private void logFailedBatch(List<OrderTable> batch) {
        Path path = Paths.get("failed_batches.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
            for (OrderTable order : batch) {
                writer.write("ID:" + order.getId().toString());
                writer.newLine();
            }
            writer.newLine();
            writer.write("----- End of Batch -----");
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void fetchAndSaveDataLoopNoFlux() {
        try {
            List<String> urlsToFetch = generateUrls();

            List<OrderTable> currentBatch = new ArrayList<>();
            int batchSize = 10_000;

            for (String url : urlsToFetch) {
                List<OrderTable> ordersFromUrl = fetchDataFromUrlNoFlux(url);
                currentBatch.addAll(ordersFromUrl);

                if (currentBatch.size() >= batchSize) {
                    orderRepository.saveAll(currentBatch);
                    currentBatch.clear();
                }
            }

            if (!currentBatch.isEmpty()) {
                orderRepository.saveAll(currentBatch);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private List<OrderTable> fetchDataFromUrlNoFlux(String url) {
        String responseJson = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(responseJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        JsonNode contentNode = rootNode.get("content");
        List<OrderTable> orders = new ArrayList<>();
        if (contentNode.isArray()) {
            for (JsonNode node : contentNode) {
                try {
                    OrderTable order = objectMapper.treeToValue(node, OrderTable.class);
                    orders.add(order);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
        return orders;
    }

}
