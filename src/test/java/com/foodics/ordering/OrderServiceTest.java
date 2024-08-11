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
        firestoreTesting.collection(Constants.PRODUCT_COLLECTION_NAME).document("1").set(product).get();

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
        assertEquals(80, updatedOnion.getQuantity());
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