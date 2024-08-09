package com.foodics.ordering.controller;

import com.foodics.ordering.model.Ingredient;
import com.foodics.ordering.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/stock")
@Tag(name = "Stock Controller", description = "Controller responsible for managing ingredient stock")
public class StockController {

    @Autowired
    StockService stockService;

    @PostMapping()
    @Operation(summary = "Add stock for ingredients", description = "Takes input a list of stock each composed of ingredient name and quality (in grams)")
    public ResponseEntity addStock(@RequestBody List<Ingredient> stocks) {
        stockService.addStock(stocks);
        return new ResponseEntity(HttpStatus.OK);
    }
}
