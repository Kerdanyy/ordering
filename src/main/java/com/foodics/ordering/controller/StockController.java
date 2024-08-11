package com.foodics.ordering.controller;

import com.foodics.ordering.model.Stock;
import com.foodics.ordering.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/stock")
@Tag(name = "Stock Controller", description = "Controller responsible for managing ingredient stocks")
public class StockController {

    @Autowired
    StockService stockService;

    @GetMapping()
    @Operation(summary = "Get all ingredients stocks", description = "Get all available ingredients stocks")
    public ResponseEntity<List<Stock>> getAllStocks() {
        return new ResponseEntity<>(stockService.getAllStocks(), HttpStatus.OK);
    }

    @PostMapping()
    @Operation(summary = "Add stocks of ingredients", description = "Add new stocks of ingredients")
    public ResponseEntity<Void> addStocks(@RequestBody List<Stock> stocks) {
        stockService.addStocks(stocks);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
