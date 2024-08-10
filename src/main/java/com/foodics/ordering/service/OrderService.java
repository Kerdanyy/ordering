package com.foodics.ordering.service;

import com.foodics.ordering.Constants;
import com.foodics.ordering.model.Ingredient;
import com.foodics.ordering.model.Order;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    Firestore firestore;

    @SneakyThrows
    public void addOrder(Order order) {
        List<Ingredient> newStock = new ArrayList<>();
        for (int i = 0; i < order.getProduct().getQuantity(); i++) {
            DocumentSnapshot beef = firestore.collection(Constants.STOCK_COLLECTION_NAME).document("Beef").get().get();
            DocumentSnapshot cheese = firestore.collection(Constants.STOCK_COLLECTION_NAME).document("Cheese").get().get();
            DocumentSnapshot onion = firestore.collection(Constants.STOCK_COLLECTION_NAME).document("Onion").get().get();
            newStock.add(adjustStockQuantity(beef));
            newStock.add(adjustStockQuantity(cheese));
            newStock.add(adjustStockQuantity(onion));
        }
        firestore.collection(Constants.STOCK_COLLECTION_NAME).document().set(newStock);
        firestore.collection(Constants.ORDER_COLLECTION_NAME).document().set(order);
    }

    private Ingredient adjustStockQuantity(DocumentSnapshot documentSnapshot) {
        if (!documentSnapshot.exists()) {
            throw new AssertionError("Order cannot be completed as ingredients are missing");
        }
        Ingredient ingredient = documentSnapshot.toObject(Ingredient.class);
        switch (ingredient.getName()) {
            case ("Beef") -> ingredient.setQuantity(ingredient.getQuantity() - 150);
            case ("Cheese") -> ingredient.setQuantity(ingredient.getQuantity() - 30);
            case ("Onion") -> ingredient.setQuantity(ingredient.getQuantity() - 20);
        }
        if (ingredient.getQuantity() < 0) {
            throw new AssertionError("Order cannot be completed as ingredient: " + ingredient.getName() + "stock is not enough");
        }
        return ingredient;
    }
}
