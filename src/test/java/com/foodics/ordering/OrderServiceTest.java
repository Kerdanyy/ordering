package com.foodics.ordering;

import com.foodics.ordering.exception.FirestoreException;
import com.foodics.ordering.exception.ValidationException;
import com.foodics.ordering.model.Ingredient;
import com.foodics.ordering.model.Order;
import com.foodics.ordering.model.OrderProduct;
import com.foodics.ordering.model.Product;
import com.foodics.ordering.service.OrderService;
import com.google.cloud.firestore.Firestore;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    @Qualifier("firestoreTesting")
    private Firestore firestoreTesting;


    @BeforeEach
    void setUp() {
        // Clear firestore before each test
        clearFirestore();
    }

    @AfterEach
    void tearDown() {
        // Clear up firestore after each test
        clearFirestore();
    }

    @Test
    void testAddOrder_Success() throws Exception {
        // Arrange
        Ingredient beef = new Ingredient("Beef", 500, 500, false);
        Ingredient cheese = new Ingredient("Cheese", 200, 200, false);
        firestoreTesting.collection(Constants.INGREDIENT_COLLECTION_NAME).document("Beef").set(beef).get();
        firestoreTesting.collection(Constants.INGREDIENT_COLLECTION_NAME).document("Cheese").set(cheese).get();

        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put("Beef", 150);
        ingredients.put("Cheese", 30);
        Product product = new Product("1", "Burger", ingredients);
        firestoreTesting.collection(Constants.PRODUCT_COLLECTION_NAME).document("1").set(product).get();

        OrderProduct orderProduct = new OrderProduct("1", 1);
        Order order = new Order(List.of(orderProduct));

        // Act
        orderService.addOrder(order);

        // Assert
        Ingredient updatedBeef = firestoreTesting.collection(Constants.INGREDIENT_COLLECTION_NAME).document("Beef").get().get().toObject(Ingredient.class);
        Ingredient updatedCheese = firestoreTesting.collection(Constants.INGREDIENT_COLLECTION_NAME).document("Cheese").get().get().toObject(Ingredient.class);

        assertEquals(350, updatedBeef.getQuantity());
        assertEquals(170, updatedCheese.getQuantity());
    }

    @Test
    @SneakyThrows
    void testAddOrder_InsufficientStock_ThrowsException() {
        // Arrange
        Ingredient beef = new Ingredient("Beef", 100, 100, false);
        firestoreTesting.collection(Constants.INGREDIENT_COLLECTION_NAME).document("Beef").set(beef).get();

        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put("Beef", 150);
        Product product = new Product("1", "Burger", ingredients);
        firestoreTesting.collection(Constants.PRODUCT_COLLECTION_NAME).document("1").set(product).get();

        OrderProduct orderProduct = new OrderProduct("1", 1);
        Order order = new Order(List.of(orderProduct));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> orderService.addOrder(order));
        assertEquals("Order cannot be completed as Beef ingredient stock is not enough", exception.getMessage());
    }

    private void clearFirestore() {
        firestoreTesting.listCollections().forEach(collectionRef -> {
            collectionRef.listDocuments().forEach(docRef -> {
                try {
                    docRef.delete().get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new FirestoreException(e);
                } catch (ExecutionException e) {
                    throw new FirestoreException(e);
                }
            });
        });
    }
}