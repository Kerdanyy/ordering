package com.foodics.ordering.service;

import com.foodics.ordering.Constants;
import com.foodics.ordering.exception.ValidationException;
import com.foodics.ordering.model.Ingredient;
import com.foodics.ordering.model.Order;
import com.foodics.ordering.model.OrderProduct;
import com.foodics.ordering.model.Product;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Transaction;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
        Map<String, Ingredient> newStock = new HashMap<>();
        try {
            firestore.runTransaction((Transaction transaction) -> {
                Iterable<DocumentReference> currentStockListDocRef = firestore.collection(Constants.INGREDIENT_COLLECTION_NAME).listDocuments();
                for (DocumentReference currentIngredientStockRef : currentStockListDocRef) {
                    Ingredient currentIngredient = transaction.get(currentIngredientStockRef).get().toObject(Ingredient.class);
                    for (OrderProduct orderProduct : order.getProducts()) {
                        DocumentSnapshot productSnapshot = transaction.get(firestore.collection(Constants.PRODUCT_COLLECTION_NAME).document(orderProduct.getProductId())).get();
                        if (!productSnapshot.exists()) {
                            throw new ValidationException("Product with id " + orderProduct.getProductId() + " does not exist");
                        }
                        for (int i = 0; i < orderProduct.getQuantity(); i++) {
                            Product product = productSnapshot.toObject(Product.class);
                            adjustStockQuantity(currentIngredient, product, newStock);
                        }
                    }
                }
                newStock.forEach((ingredientName, ingredient) -> transaction.set(firestore.collection(Constants.INGREDIENT_COLLECTION_NAME).document(ingredientName), ingredient));
                transaction.set(firestore.collection(Constants.ORDER_COLLECTION_NAME).document(), order);
                return null;
            }).get();
        } catch (ExecutionException ex) {
            throw ex.getCause();
        }
    }

    private void adjustStockQuantity(Ingredient ingredient, Product product, Map<String, Ingredient> newStock) {
        ingredient.setQuantity(ingredient.getQuantity() - product.getIngredients().get(ingredient.getName()));
        if (!ingredient.isNotificationSent() && ingredient.getQuantity() <= ingredient.getInitialQuantity() / 2) {
            emailService.sendEmail(toEmail, "Ingredient Stock Notification", ingredient.getName() + " ingredient stock reached 50%");
            ingredient.setNotificationSent(true);
        }
        newStock.put(ingredient.getName(), ingredient);
        if (ingredient.getQuantity() < 0) {
            throw new ValidationException("Order cannot be completed as " + ingredient.getName() + " ingredient stock is not enough");
        }
    }

    @SneakyThrows
    public List<Order> getAllOrders() {
        return firestore.collection(Constants.ORDER_COLLECTION_NAME).get().get().toObjects(Order.class);
    }
}
