package com.foodics.ordering.service;

import com.foodics.ordering.Constants;
import com.foodics.ordering.model.Ingredient;
import com.foodics.ordering.model.Order;
import com.foodics.ordering.model.Product;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Transaction;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class OrderService {

    @Autowired
    Firestore firestore;

    @Autowired
    EmailService emailService;

    @Value("${email.to}")
    private String toEmail;

    @SneakyThrows
    public void addOrder(Order order) {
        HashMap<String, Ingredient> newStock = new HashMap<>();
        firestore.runTransaction((Transaction transaction) -> {
            Iterable<DocumentReference> currentStock = firestore.collection(Constants.STOCK_COLLECTION_NAME).listDocuments();
            for (DocumentReference docRef : currentStock) {
                Ingredient currentIngredientStock = transaction.get(docRef).get().toObject(Ingredient.class);
                for (Product product : order.getProducts()) {
                    if (product.getId() != 1) {
                        throw new IllegalArgumentException("Only product with ID 1 is supported currently");
                    }
                    for (int i = 0; i < product.getQuantity(); i++) {
                        adjustStockQuantity(currentIngredientStock, newStock);
                    }
                }
            }
            newStock.forEach((ingredientName, ingredient) -> transaction.set(firestore.collection(Constants.STOCK_COLLECTION_NAME).document(ingredientName), ingredient));
            transaction.set(firestore.collection(Constants.ORDER_COLLECTION_NAME).document(), order);
            return null;
        }).get();
    }

    private void adjustStockQuantity(Ingredient ingredient, HashMap<String, Ingredient> newStock) {
        switch (ingredient.getName()) {
            case ("Beef") -> ingredient.setCurrentQuantity(ingredient.getCurrentQuantity() - 150);
            case ("Cheese") -> ingredient.setCurrentQuantity(ingredient.getCurrentQuantity() - 30);
            case ("Onion") -> ingredient.setCurrentQuantity(ingredient.getCurrentQuantity() - 20);
            default -> throw new IllegalArgumentException("Unsupported ingredient: " + ingredient.getName());
        }
        newStock.put(ingredient.getName(), ingredient);
        if (ingredient.getCurrentQuantity() <= ingredient.getInitialQuantity() / 2) {
            emailService.sendEmail(toEmail, "Ingredient stock notification", "Ingredient: " + ingredient.getName() + "stock below or equal 50%");
        }
        if (ingredient.getCurrentQuantity() < 0) {
            throw new IllegalArgumentException("Order cannot be completed as ingredient: " + ingredient.getName() + "stock is not enough");
        }
    }
}
