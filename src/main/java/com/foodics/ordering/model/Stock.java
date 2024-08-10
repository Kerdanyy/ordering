package com.foodics.ordering.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;

@Data
public class Stock {
    @DocumentId
    private String name;
    private int quantity;
    private int initialQuantity;
    private boolean notificationSent;
}
