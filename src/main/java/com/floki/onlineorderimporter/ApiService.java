package com.floki.onlineorderimporter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.floki.onlineorderimporter.model.OrderTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApiService {
    private final WebClient webClient = WebClient.create("https://kata-espublicotech.g3stiona.com/v1/"); // Reemplaza con la URL base de tu API


    @Autowired
    private OrderRepository orderRepository;

    public OrderTable saveOrder(OrderTable order) {
        return orderRepository.save(order);
    }

    public String fetchData() {
        String uri = UriComponentsBuilder.fromPath("/orders")
                .queryParam("page", 1)
                .queryParam("max-per-page", 5)
                .toUriString();
        return webClient.get()  // Método HTTP GET
                .uri(uri) // Endpoint específico de la API
                .retrieve()  // Recupera la respuesta
                .bodyToMono(String.class)  // Convierte la respuesta a String (puedes cambiar esto según tus necesidades)
                .block();  // Espera la respuesta (bloqueante)

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
        objectMapper.registerModule(new JavaTimeModule());  // Registra el módulo para Java 8 date/time
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
        } finally {

        }

        return savedOrders;
    }


    public List<OrderTable> fetchAndSaveDataLoopOld() {
        try {
//            String uri = UriComponentsBuilder.fromPath("/orders")
//                .queryParam("page", 1)
//                .queryParam("max-per-page", 500)
//                .toUriString();
            String nextUrl = "https://kata-espublicotech.g3stiona.com/v1/orders?page=1&max-per-page=500";



//            String uri = UriComponentsBuilder.fromPath(nextUrl)
//                    .toUriString();
            List<OrderTable> savedOrders = new ArrayList<>();

            while (nextUrl != null) {
        String responseJson = webClient.get()
                .uri(nextUrl)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // Registra el módulo para Java 8 date/time

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
        } finally {

        }

    }


    public void fetchAndSaveDataLoop() {
        try {
            List<OrderTable> savedOrders = new ArrayList<>();
            List<String> urlsToFetch = generateUrls();  // Genera las URLs para las peticiones en paralelo

            // Realiza las peticiones en paralelo usando Flux
            Flux.fromIterable(urlsToFetch)
                    .parallel()
                    .runOn(Schedulers.boundedElastic())
                    .flatMap(this::fetchDataFromUrl)
                    .sequential()
                    .collectList()
                    .block()
                    .forEach(savedOrders::add);
            // Guarda todos los datos en la base de datos en lotes
            orderRepository.saveAll(savedOrders);

//            return Flux.fromIterable(savedOrders);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> generateUrls() {
        // Aquí, genera las URLs para las peticiones en paralelo basado en tu lógica
        // Por ejemplo, si sabes que siempre vas a hacer 10 peticiones, genera 10 URLs con diferentes páginas
        List<String> urls = new ArrayList<>();
        for (int i = 1; i <= 2000; i++) {
            urls.add("https://kata-espublicotech.g3stiona.com/v1/orders?page=" + i + "&max-per-page=500");
        }
        return urls;
    }

    private Flux<OrderTable> fetchDataFromUrl(String url) {
        // Aquí, pon tu lógica para hacer una petición y convertir la respuesta a una lista de OrderTable
        // Este método se llamará en paralelo para cada URL
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



}
