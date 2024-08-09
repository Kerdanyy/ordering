package com.foodics.ordering.model;

import lombok.Data;

@Data
public class Product {
    private String name;
    private int quantity;
    private Ingredient ingredient;
}
