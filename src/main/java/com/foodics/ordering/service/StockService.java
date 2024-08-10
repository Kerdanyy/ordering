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
        stocks.forEach(ingredient -> {
            ingredient.setInitialQuantity(ingredient.getCurrentQuantity());
            firestore.collection(Constants.STOCK_COLLECTION_NAME).document(ingredient.getName()).set(ingredient);
        });
    }

    public List<Ingredient> getAllStock() {
        return StreamSupport.stream(firestore.collection(Constants.STOCK_COLLECTION_NAME).listDocuments().spliterator(), false).map(docRef -> {
            try {
                return docRef.get().get().toObject(Ingredient.class);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }
}
