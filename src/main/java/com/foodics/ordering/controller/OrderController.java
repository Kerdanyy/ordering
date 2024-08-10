package com.foodics.ordering.controller;

import com.foodics.ordering.model.Order;
import com.foodics.ordering.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/order")
@Tag(name = "Order Controller", description = "Controller responsible for managing order")
public class OrderController {

    @Autowired
    OrderService orderService;

    @PostMapping()
    @Operation(summary = "Add order", description = "Takes input an order containing list of products")
    public ResponseEntity addOrder(@RequestBody Order order) {
        orderService.addOrder(order);
        return new ResponseEntity(HttpStatus.OK);
    }
}
