package com.foodics.ordering.controller;

import com.foodics.ordering.model.Ingredient;
import com.foodics.ordering.service.EmailService;
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
@Tag(name = "Stock Controller", description = "Controller responsible for managing ingredient stock")
public class StockController {

    @Autowired
    StockService stockService;

    @Autowired
    EmailService emailService;

    @GetMapping()
    @Operation(summary = "Get all ingredients stock", description = "Get all available stocks each composed of ingredient name and quality (in grams)")
    public ResponseEntity<List<Ingredient>> getAllStock() {
        return new ResponseEntity<>(stockService.getAllStock(), HttpStatus.OK);
    }

    @PostMapping()
    @Operation(summary = "Add stock of ingredients", description = "Takes input a list of stock each composed of ingredient name and quality (in grams)")
    public ResponseEntity<Void> addStock(@RequestBody List<Ingredient> stocks) {
        stockService.addStock(stocks);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
