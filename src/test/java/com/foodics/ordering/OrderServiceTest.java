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

@SpringBootTest
@ActiveProfiles("testing")
//testing profile is used to switch to testing database
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private Firestore firestoreTesting;


    @BeforeEach
    void runBeforeEachTest() {
        clearDatabase();
    }

    @AfterEach
    void runAfterEachTest() {
        clearDatabase();
    }

    @Test
    void testAddOrder_Success() throws Exception {
        // Arrange
        Ingredient beef = new Ingredient("beef", 500);
        Ingredient cheese = new Ingredient("cheese", 200);
        Ingredient onion = new Ingredient("onion", 100);
        firestoreTesting.collection(Constants.INGREDIENT_COLLECTION_NAME).document("beef").set(beef).get();
        firestoreTesting.collection(Constants.INGREDIENT_COLLECTION_NAME).document("cheese").set(cheese).get();
        firestoreTesting.collection(Constants.INGREDIENT_COLLECTION_NAME).document("onion").set(onion).get();

        String productId = "1";
        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put("beef", 150);
        ingredients.put("cheese", 30);
        ingredients.put("onion", 20);
        Product product = new Product(productId, "burger", ingredients);
        firestoreTesting.collection(Constants.PRODUCT_COLLECTION_NAME).document(productId).set(product).get();

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

    @Test
    @SneakyThrows
    void testAddOrder_InsufficientStock_ThrowsException() {
        // Arrange
        Ingredient beef = new Ingredient("beef", 100);
        firestoreTesting.collection(Constants.INGREDIENT_COLLECTION_NAME).document("beef").set(beef).get();

        String productId = "2";
        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put("beef", 150);
        Product product = new Product(productId, "burger", ingredients);
        firestoreTesting.collection(Constants.PRODUCT_COLLECTION_NAME).document(productId).set(product).get();

        int productQuantity = 1;
        OrderProduct orderProduct = new OrderProduct(productId, productQuantity);
        Order order = new Order(List.of(orderProduct));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> orderService.addOrder(order));
        assertEquals("Order cannot be completed as beef ingredient stock is not enough", exception.getMessage());
    }

    @Test
    @SneakyThrows
    void testAddOrder_ProductIdNotFound_ThrowsException() {
        // Arrange
        Ingredient beef = new Ingredient("beef", 500);
        firestoreTesting.collection(Constants.INGREDIENT_COLLECTION_NAME).document("beef").set(beef).get();

        String productId = "1";
        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put("beef", 150);
        Product product2 = new Product(productId, "burger", ingredients);
        firestoreTesting.collection(Constants.PRODUCT_COLLECTION_NAME).document(productId).set(product2).get();

        String notFoundProductId = "3";
        int productQuantity = 1;
        OrderProduct orderProduct = new OrderProduct(notFoundProductId, productQuantity);
        Order order = new Order(List.of(orderProduct));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> orderService.addOrder(order));
        assertEquals("Product with id 3 does not exist", exception.getMessage());
    }

    /**
     * Clear all documents in DB
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