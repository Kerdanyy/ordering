package com.foodics.ordering.service;

import com.foodics.ordering.Constants;
import com.foodics.ordering.model.Ingredient;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.StreamSupport;

@Service
public class StockService {
    @Autowired
    Firestore firestore;

    public void addStock(List<Ingredient> stocks) {
        stocks.forEach(stock -> firestore.collection(Constants.STOCK_COLLECTION_NAME).document(stock.getName()).set(stock));
    }

    public List<Ingredient> getAllStock() {
        return StreamSupport.stream(firestore.collection(Constants.STOCK_COLLECTION_NAME).listDocuments().spliterator(), false).map(docRef -> {
            try {
                return docRef.get().get().toObject(Ingredient.class);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }
}
