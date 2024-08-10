package com.foodics.ordering.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;

import java.util.HashMap;

@Data
public class Product {
    @DocumentId
    private long id;
    private String name;
    private int quantity;
    private HashMap<String, Integer> ingredients;
}
