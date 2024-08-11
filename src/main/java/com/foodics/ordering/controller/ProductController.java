package com.foodics.ordering.controller;

import com.foodics.ordering.model.Product;
import com.foodics.ordering.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/product")
@Tag(name = "Product Controller", description = "Controller responsible for managing available products")
public class ProductController {

    @Autowired
    ProductService productService;

    @GetMapping()
    @Operation(summary = "Get all products", description = "Get all available products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return new ResponseEntity<>(productService.getAllProducts(), HttpStatus.OK);
    }

    @PostMapping()
    @Operation(summary = "Add product", description = "Add new products")
    public ResponseEntity<Void> addProducts(@RequestBody List<Product> products) {
        productService.addProducts(products);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
