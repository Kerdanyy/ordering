package com.foodics.ordering.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;

import java.util.List;

@Data
public class Order {
    @DocumentId
    private long id;
    private List<Product> products;
}
