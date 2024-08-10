package com.foodics.ordering.service;

import com.foodics.ordering.Constants;
import com.foodics.ordering.model.Ingredient;
import com.foodics.ordering.model.Order;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class OrderService {

    @Autowired
    Firestore firestore;

    @SneakyThrows
    public void addOrder(Order order) {
        HashMap<String, Ingredient> newStock = new HashMap<>();
        Iterable<DocumentReference> currentStock = firestore.collection(Constants.STOCK_COLLECTION_NAME).listDocuments();
        for (DocumentReference docRef : currentStock) {
            Ingredient currentIngredientStock = docRef.get().get().toObject(Ingredient.class);
            for (int i = 0; i < order.getProducts().size(); i++) {
                adjustStockQuantity(currentIngredientStock, newStock);
            }
        }
        newStock.forEach((ingredientName, ingredient) -> firestore.collection(Constants.STOCK_COLLECTION_NAME).document(ingredientName).set(ingredient));
        firestore.collection(Constants.ORDER_COLLECTION_NAME).document().set(order);
    }

    private void adjustStockQuantity(Ingredient ingredient, HashMap<String, Ingredient> newStock) {
        switch (ingredient.getName()) {
            case ("Beef") -> ingredient.setQuantity(ingredient.getQuantity() - 150);
            case ("Cheese") -> ingredient.setQuantity(ingredient.getQuantity() - 30);
            case ("Onion") -> ingredient.setQuantity(ingredient.getQuantity() - 20);
        }
        newStock.put(ingredient.getName(), ingredient);
        if (ingredient.getQuantity() < 0) {
            throw new AssertionError("Order cannot be completed as ingredient: " + ingredient.getName() + "stock is not enough");
        }
    }
}
