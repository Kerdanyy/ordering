package com.foodics.ordering.model;

import lombok.Data;

@Data
public class OrderItem {
    private long id;
    private int quantity;
}