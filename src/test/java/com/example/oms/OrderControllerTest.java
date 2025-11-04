package com.example.oms;

import com.example.oms.api.dto.CreateOrderRequest;
import com.example.oms.api.dto.OrderItemRequest;
import com.example.oms.api.dto.OrderResponse;
import com.example.oms.api.dto.UpdateOrderStatusRequest;
import com.example.oms.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/orders";
    }

    @Test
    void createGetListAndCancelFlow() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setCustomerId(UUID.randomUUID().toString());
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId("P1");
        item.setQuantity(2);
        req.setItems(List.of(item));

        ResponseEntity<OrderResponse> createdResp = rest.postForEntity(baseUrl(), req, OrderResponse.class);
        assertEquals(HttpStatus.CREATED, createdResp.getStatusCode());
        OrderResponse created = createdResp.getBody();
        assertNotNull(created);
        UUID id = created.getId();

        OrderResponse fetched = rest.getForObject(baseUrl() + "/" + id, OrderResponse.class);
        assertEquals(id, fetched.getId());

        ResponseEntity<OrderResponse[]> listResp = rest.getForEntity(baseUrl(), OrderResponse[].class);
        assertEquals(HttpStatus.OK, listResp.getStatusCode());
        assertTrue(listResp.getBody().length >= 1);

        OrderResponse cancelled = rest.postForObject(baseUrl() + "/" + id + "/cancel", null, OrderResponse.class);
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void updateStatusEndpoint() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setCustomerId(UUID.randomUUID().toString());
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId("P2");
        item.setQuantity(1);
        req.setItems(List.of(item));

        OrderResponse created = rest.postForEntity(baseUrl(), req, OrderResponse.class).getBody();
        assertNotNull(created);

        UpdateOrderStatusRequest patch = new UpdateOrderStatusRequest();
        patch.setStatus(OrderStatus.SHIPPED);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<OrderResponse> patched = rest.exchange(
                baseUrl() + "/" + created.getId() + "/status",
                HttpMethod.PUT,
                new HttpEntity<>(patch, headers),
                OrderResponse.class
        );
        assertEquals(HttpStatus.OK, patched.getStatusCode());
        assertEquals(OrderStatus.SHIPPED, patched.getBody().getStatus());
    }

    @Test
    void cancelNonPendingShouldFail() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setCustomerId(UUID.randomUUID().toString());
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId("P3");
        item.setQuantity(1);
        req.setItems(List.of(item));

        OrderResponse created = rest.postForEntity(baseUrl(), req, OrderResponse.class).getBody();
        assertNotNull(created);

        UpdateOrderStatusRequest patch = new UpdateOrderStatusRequest();
        patch.setStatus(OrderStatus.PROCESSING);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        rest.exchange(baseUrl() + "/" + created.getId() + "/status", HttpMethod.PUT, new HttpEntity<>(patch, headers), OrderResponse.class);

        ResponseEntity<Map> resp = rest.postForEntity(baseUrl() + "/" + created.getId() + "/cancel", null, Map.class);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }
}


