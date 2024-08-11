package com.foodics.ordering.controller;

import com.foodics.ordering.model.Ingredient;
import com.foodics.ordering.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/ingredient")
@Tag(name = "Stock Controller", description = "Controller responsible for managing ingredient stocks")
public class IngredientController {

    @Autowired
    IngredientService ingredientService;

    @GetMapping()
    @Operation(summary = "Get all ingredients stocks", description = "Get all available ingredients stocks")
    public ResponseEntity<List<Ingredient>> getAllStocks() {
        return new ResponseEntity<>(ingredientService.getAllStocks(), HttpStatus.OK);
    }

    @PostMapping()
    @Operation(summary = "Add ingredients stocks", description = "Add new ingredients stocks")
    public ResponseEntity<Void> addStocks(@RequestBody List<Ingredient> ingredients) {
        ingredientService.addStocks(ingredients);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
