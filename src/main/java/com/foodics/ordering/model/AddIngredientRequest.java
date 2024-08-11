package com.foodics.ordering.model;

import lombok.Data;

@Data
public class AddIngredientRequest {
    private String name;
    private int quantity; //in grams
}
