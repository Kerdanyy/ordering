package com.foodics.ordering.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class Product {
    @DocumentId
    private String id;
    private String name;
    private Map<String, Integer> ingredients;
}
