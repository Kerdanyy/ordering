package com.foodics.ordering;

import com.foodics.ordering.exception.FirestoreException;
import com.foodics.ordering.exception.ValidationException;
import com.foodics.ordering.model.Ingredient;
import com.foodics.ordering.model.Order;
import com.foodics.ordering.model.OrderProduct;
import com.foodics.ordering.model.Product;
import com.foodics.ordering.service.OrderService;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the {@link OrderService} class.
 * <p>
 * This class tests the functionality of the {@link OrderService}
 * It includes tests for successful order addition, insufficient stock, and product ID not found scenarios.
 * "testing" profile is used to connect to a testing database
 * </p>
 */
@SpringBootTest
@ActiveProfiles("testing")
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private Firestore firestoreTesting;

    /**
     * Sets up the test environment by clearing the Firestore database before each test.
     */
    @BeforeEach
    void runBeforeEachTest() {
        clearDatabase();
    }

    /**
     * Cleans up the test environment by clearing the Firestore database after each test.
     */
    @AfterEach
    void runAfterEachTest() {
        clearDatabase();
    }

    /**
     * Tests the successful addition of an order.
     * <p>
     * This test verifies that after adding an order, the stock quantities are updated correctly and
     * the order is stored in the database.
     * </p>
     */
    @Test
    void testAddOrder_Success() throws Exception {
        // Arrange
        WriteBatch batch = firestoreTesting.batch();
        Ingredient beef = new Ingredient("beef", 500);
        Ingredient cheese = new Ingredient("cheese", 200);
        Ingredient onion = new Ingredient("onion", 100);
        batch.set(firestoreTesting.collection(Constants.INGREDIENT_COLLECTION_NAME).document("beef"), beef);
        batch.set(firestoreTesting.collection(Constants.INGREDIENT_COLLECTION_NAME).document("cheese"), cheese);
        batch.set(firestoreTesting.collection(Constants.INGREDIENT_COLLECTION_NAME).document("onion"), onion);

        String productId = "1";
        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put("beef", 150);
        ingredients.put("cheese", 30);
        ingredients.put("onion", 20);
        Product product = new Product(productId, "burger", ingredients);
        batch.set(firestoreTesting.collection(Constants.PRODUCT_COLLECTION_NAME).document(productId), product);

        batch.commit().get();

        int productQuantity = 1;
        OrderProduct orderProduct = new OrderProduct(productId, productQuantity);
        Order order = new Order(List.of(orderProduct));

        // Act
        orderService.addOrder(order);

        // Assert
        Ingredient updatedBeef = firestoreTesting.collection(Constants.INGREDIENT_COLLECTION_NAME).document("beef").get().get().toObject(Ingredient.class);
        Ingredient updatedCheese = firestoreTesting.collection(Constants.INGREDIENT_COLLECTION_NAME).document("cheese").get().get().toObject(Ingredient.class);
        Ingredient updatedOnion = firestoreTesting.collection(Constants.INGREDIENT_COLLECTION_NAME).document("onion").get().get().toObject(Ingredient.class);

        assertEquals(350, updatedBeef.getQuantity());
        assertEquals(170, updatedCheese.getQuantity());
        assertEquals(80, updatedOnion.getQuantity(), "");

        List<Order> storedOrders = firestoreTesting.collection(Constants.ORDER_COLLECTION_NAME).get().get().toObjects(Order.class);
        Order storedOrder = storedOrders.get(0);
        assertEquals(order, storedOrder);
    }

    /**
     * Tests the behavior when there is insufficient stock to fulfill an order.
     * <p>
     * This test verifies that a {@link ValidationException} is thrown if the stock is insufficient for
     * the requested order.
     * </p>
     */
    @Test
    @SneakyThrows
    void testAddOrder_InsufficientStock_ThrowsException() {
        // Arrange
        WriteBatch batch = firestoreTesting.batch();
        Ingredient beef = new Ingredient("beef", 100);
        batch.set(firestoreTesting.collection(Constants.INGREDIENT_COLLECTION_NAME).document("beef"), beef);

        String productId = "2";
        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put("beef", 150);
        Product product = new Product(productId, "burger", ingredients);
        batch.set(firestoreTesting.collection(Constants.PRODUCT_COLLECTION_NAME).document(productId), product);

        batch.commit().get();

        int productQuantity = 1;
        OrderProduct orderProduct = new OrderProduct(productId, productQuantity);
        Order order = new Order(List.of(orderProduct));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> orderService.addOrder(order));
        assertEquals("Order cannot be completed as beef ingredient stock is not enough", exception.getMessage());
    }

    /**
     * Tests the behavior when attempting to add an order with a non-existent product ID.
     * <p>
     * This test verifies that a {@link ValidationException} is thrown if the product ID in the order
     * does not exist in the database.
     * </p>
     */
    @Test
    @SneakyThrows
    void testAddOrder_ProductIdNotFound_ThrowsException() {
        // Arrange
        WriteBatch batch = firestoreTesting.batch();
        Ingredient beef = new Ingredient("beef", 500);
        batch.set(firestoreTesting.collection(Constants.INGREDIENT_COLLECTION_NAME).document("beef"), beef);

        String productId = "3";
        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put("beef", 150);
        Product product = new Product(productId, "burger", ingredients);
        batch.set(firestoreTesting.collection(Constants.PRODUCT_COLLECTION_NAME).document(productId), product);

        batch.commit().get();

        String notFoundProductId = "100";
        int productQuantity = 1;
        OrderProduct orderProduct = new OrderProduct(notFoundProductId, productQuantity);
        Order order = new Order(List.of(orderProduct));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> orderService.addOrder(order));
        assertEquals("Product with id " + notFoundProductId + " does not exist", exception.getMessage());
    }

    /**
     * Clears all documents in the Firestore database for testing purposes.
     * <p>
     * This method iterates through all collections and documents in the Firestore instance
     * and deletes them in batches.
     * </p>
     *
     * @throws FirestoreException If an error occurs while deleting documents.
     */
    private void clearDatabase() {
        firestoreTesting.listCollections().forEach(collectionRef -> {
            try {
                WriteBatch batch = firestoreTesting.batch();
                collectionRef.listDocuments().forEach(docRef -> {
                    batch.delete(docRef);
                });
                batch.commit().get();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new FirestoreException(e);
            }
        });
    }
}
