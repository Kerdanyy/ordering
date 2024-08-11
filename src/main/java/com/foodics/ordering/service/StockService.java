package com.foodics.ordering.service;

import com.foodics.ordering.Constants;
import com.foodics.ordering.exception.FirestoreException;
import com.foodics.ordering.model.Stock;
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

    public void addStock(List<Stock> stocks) {
        stocks.forEach(ingredientStock -> {
            ingredientStock.setInitialQuantity(ingredientStock.getQuantity());
            firestore.collection(Constants.STOCK_COLLECTION_NAME).document(ingredientStock.getName()).set(ingredientStock);
        });
    }

    public List<Stock> getAllStock() {
        return StreamSupport.stream(firestore.collection(Constants.STOCK_COLLECTION_NAME).listDocuments().spliterator(), false).map(docRef -> {
            try {
                return docRef.get().get().toObject(Stock.class);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new FirestoreException(e);
            } catch (ExecutionException e) {
                throw new FirestoreException(e);
            }
        }).toList();
    }
}
