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

    /**
     * Adds a new order to the DB. The method processes the order by adjusting the ingredient stock
     * quantities based on the order's products.
     *
     * @param order The order to be added to the database. The order contains a list of products, and each product has a
     *              specified quantity that will be used to adjust the ingredient stock.
     * @throws ValidationException If a product in the order does not exist
     */
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

    /**
     * Adjusts the stock quantity of a given ingredient based on the specified product. Updates the ingredient's quantity
     * and checks if a notification should be sent if the stock falls below a certain threshold.
     *
     * @param ingredient The ingredient whose stock quantity needs to be adjusted.
     * @param product    The product containing the ingredients and their quantities that will be used to adjust the stock.
     * @param newStock   A map that stores the updated ingredients with their new stock quantities. The key is the ingredient
     *                   name and the value is the updated {@link Ingredient}.
     * @throws ValidationException If the resulting stock quantity of the ingredient is negative, indicating insufficient
     *                             stock to complete the order.
     */
    private void adjustStockQuantity(Ingredient ingredient, Product product, Map<String, Ingredient> newStock) {
        ingredient.setQuantity(ingredient.getQuantity() - product.getIngredients().get(ingredient.getName()));
        if (ingredient.getQuantity() < 0) {
            throw new ValidationException("Order cannot be completed as " + ingredient.getName() + " ingredient stock is not enough");
        }
        newStock.put(ingredient.getName(), ingredient);
        if (!ingredient.isNotificationSent() && ingredient.getQuantity() <= ingredient.getInitialQuantity() / 2) {
            emailService.sendEmail(toEmail, "Ingredient Stock Notification", ingredient.getName() + " ingredient stock reached 50%");
            ingredient.setNotificationSent(true);
        }
    }

    /**
     * Get all orders in DB
     *
     * @return A list of {@link Order}
     */
    @SneakyThrows
    public List<Order> getAllOrders() {
        return firestore.collection(Constants.ORDER_COLLECTION_NAME).get().get().toObjects(Order.class);
    }
}
