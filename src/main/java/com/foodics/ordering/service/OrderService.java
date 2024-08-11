package com.foodics.ordering.service;

import com.foodics.ordering.Constants;
import com.foodics.ordering.exception.ValidationException;
import com.foodics.ordering.model.Order;
import com.foodics.ordering.model.OrderProduct;
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
                for (OrderProduct orderProduct : order.getProducts()) {
                    DocumentSnapshot productSnapshot = transaction.get(firestore.collection(Constants.PRODUCT_COLLECTION_NAME).document(String.valueOf(orderProduct.getId()))).get();
                    if (!productSnapshot.exists()) {
                        throw new ValidationException("Product with id " + orderProduct.getId() + " does not exist");
                    }
                    for (int i = 0; i < orderProduct.getQuantity(); i++) {
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
            emailService.sendEmail(toEmail, "Ingredient Stock Notification", ingredientStock.getName() + " ingredient stock reached 50%");
            ingredientStock.setNotificationSent(true);
        }
        newStock.put(ingredientStock.getName(), ingredientStock);
        if (ingredientStock.getQuantity() < 0) {
            throw new ValidationException("Order cannot be completed as " + ingredientStock.getName() + " ingredient stock is not enough");
        }
    }
}
