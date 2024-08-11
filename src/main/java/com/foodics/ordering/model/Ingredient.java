package com.foodics.ordering.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Ingredient {
    @DocumentId
    private String name;
    private int quantity; //in grams
    private int initialQuantity;
    private boolean notificationSent;
}
