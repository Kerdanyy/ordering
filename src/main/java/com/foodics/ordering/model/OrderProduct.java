package com.foodics.ordering.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderProduct {
    private String id;
    private int quantity;
}