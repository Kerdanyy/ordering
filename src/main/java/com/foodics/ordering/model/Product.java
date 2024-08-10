package com.foodics.ordering.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;

import java.util.HashMap;

@Data
public class Product {
    @DocumentId
    private String id;
    private String name;
    private HashMap<String, Integer> ingredients;
}
