package com.foodics.ordering.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;

@Data
public class Ingredient {
    @DocumentId
    private String name;
    private int quantity;
}
