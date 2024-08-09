package com.foodics.ordering.service;

import com.foodics.ordering.model.Stock;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {

    private static final String STOCK_COLLECTION_NAME = "stock";

    @Autowired
    Firestore firestore;

    public void addStock(List<Stock> stocks) {
        stocks.forEach(stock -> firestore.collection(STOCK_COLLECTION_NAME).document(stock.getIngredient()).set(stock));
    }
}
