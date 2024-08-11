package com.foodics.ordering.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Ingredient {
    @DocumentId
    private String name;
    private int quantity; //in grams
    private int initialQuantity;
    private boolean notificationSent;

    public Ingredient(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }
}


