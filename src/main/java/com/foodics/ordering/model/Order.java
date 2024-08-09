package com.foodics.ordering.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;

@Data
public class Order {
    @DocumentId
    private long id;
    private Product product;
}
