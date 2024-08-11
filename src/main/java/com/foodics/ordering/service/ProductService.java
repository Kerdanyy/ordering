package com.foodics.ordering.service;

import com.foodics.ordering.Constants;
import com.foodics.ordering.exception.FirestoreException;
import com.foodics.ordering.model.Product;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.StreamSupport;

@Service
public class ProductService {
    @Autowired
    Firestore firestore;

    public void addProducts(List<Product> products) {
        products.forEach(product -> {
            firestore.collection(Constants.PRODUCT_COLLECTION_NAME).document(product.getId()).set(product);
        });
    }

    public List<Product> getAllProducts() {
        return StreamSupport.stream(firestore.collection(Constants.PRODUCT_COLLECTION_NAME).listDocuments().spliterator(), false).map(docRef -> {
            try {
                return docRef.get().get().toObject(Product.class);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new FirestoreException(e);
            } catch (ExecutionException e) {
                throw new FirestoreException(e);
            }
        }).toList();
    }
}
