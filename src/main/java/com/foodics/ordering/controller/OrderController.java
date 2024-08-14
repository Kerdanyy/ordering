package com.foodics.ordering.controller;

import com.foodics.ordering.model.Order;
import com.foodics.ordering.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/order")
@Tag(name = "Order Controller", description = "Controller responsible for managing orders")
public class OrderController {

    @Autowired
    OrderService orderService;

    @GetMapping()
    @Operation(summary = "Get all orders", description = "Get all available orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return new ResponseEntity<>(orderService.getAllOrders(), HttpStatus.OK);
    }

    @PostMapping()
    @Operation(summary = "Add order", description = "Adds new order and returns the order ID")
    public ResponseEntity<String> addOrder(@RequestBody Order order) {
        return new ResponseEntity<>(orderService.addOrder(order), HttpStatus.OK);
    }
}
