package com.foodics.ordering.service;

import com.foodics.ordering.Constants;
import com.foodics.ordering.model.Order;
import com.foodics.ordering.model.OrderItem;
import com.foodics.ordering.model.Product;
import com.foodics.ordering.model.Stock;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
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
        HashMap<String, Stock> newStock = new HashMap<>();
        firestore.runTransaction((Transaction transaction) -> {
            Iterable<DocumentReference> currentStockListDocRef = firestore.collection(Constants.STOCK_COLLECTION_NAME).listDocuments();
            for (DocumentReference currentIngredientStockRef : currentStockListDocRef) {
                Stock currentIngredientStock = transaction.get(currentIngredientStockRef).get().toObject(Stock.class);
                for (OrderItem orderItem : order.getProducts()) {
                    DocumentSnapshot productSnapshot = transaction.get(firestore.collection(Constants.PRODUCT_COLLECTION_NAME).document(String.valueOf(orderItem.id()))).get();
                    if (!productSnapshot.exists()) {
                        throw new IllegalArgumentException("Only product with ID 1 is available currently");
                    }
                    for (int i = 0; i < orderItem.quantity(); i++) {
                        Product product = productSnapshot.toObject(Product.class);
                        adjustStockQuantity(currentIngredientStock, product, newStock);
                    }
                }
            }
            newStock.forEach((ingredientName, ingredientStock) -> transaction.set(firestore.collection(Constants.STOCK_COLLECTION_NAME).document(ingredientName), ingredientStock));
            transaction.set(firestore.collection(Constants.ORDER_COLLECTION_NAME).document(), order);
            return null;
        }).get();
    }

    private void adjustStockQuantity(Stock ingredientStock, Product product, HashMap<String, Stock> newStock) {
        ingredientStock.setQuantity(ingredientStock.getQuantity() - product.getIngredients().get(ingredientStock.getName()));
        if (!ingredientStock.isNotificationSent() && ingredientStock.getQuantity() <= ingredientStock.getInitialQuantity() / 2) {
            emailService.sendEmail(toEmail, "Ingredient stock notification", ingredientStock.getName() + " ingredient stock reached 50%");
            ingredientStock.setNotificationSent(true);
        }
        newStock.put(ingredientStock.getName(), ingredientStock);
        if (ingredientStock.getQuantity() < 0) {
            throw new IllegalArgumentException("Order cannot be completed as " + ingredientStock.getName() + " ingredient stock is not enough");
        }
    }
}
