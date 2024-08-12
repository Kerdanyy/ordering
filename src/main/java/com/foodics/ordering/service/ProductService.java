package com.foodics.ordering.service;

import com.foodics.ordering.Constants;
import com.foodics.ordering.model.Product;
import com.google.cloud.firestore.Firestore;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    @Autowired
    Firestore firestore;

    /**
     * Add products to DB
     *
     * @param products
     */
    public void addProducts(List<Product> products) {
        products.forEach(product -> firestore.collection(Constants.PRODUCT_COLLECTION_NAME).document(product.getId()).set(product));
    }

    /**
     * Get all products in DB
     *
     * @return A list of {@link Product} object
     */
    @SneakyThrows
    public List<Product> getAllProducts() {
        return firestore.collection(Constants.PRODUCT_COLLECTION_NAME).get().get().toObjects(Product.class);
    }
}
