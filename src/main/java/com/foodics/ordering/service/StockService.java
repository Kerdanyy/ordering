package com.foodics.ordering.service;

import com.foodics.ordering.Constants;
import com.foodics.ordering.model.Ingredient;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {
    @Autowired
    Firestore firestore;

    public void addStock(List<Ingredient> stocks) {
        stocks.forEach(stock -> firestore.collection(Constants.STOCK_COLLECTION_NAME).document().set(stock));
    }
}
