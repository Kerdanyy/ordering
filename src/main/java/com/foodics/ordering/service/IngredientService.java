package com.foodics.ordering.service;

import com.foodics.ordering.Constants;
import com.foodics.ordering.model.AddIngredientRequest;
import com.foodics.ordering.model.Ingredient;
import com.google.cloud.firestore.Firestore;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngredientService {
    @Autowired
    Firestore firestore;

    /**
     * Add ingredients in DB
     *
     * @param ingredients A list of {@link AddIngredientRequest}
     */
    public void addIngredients(List<AddIngredientRequest> ingredients) {
        ingredients.forEach(addIngredientRequest -> {
            Ingredient ingredient = new Ingredient(addIngredientRequest.getName(), addIngredientRequest.getQuantity());
            firestore.collection(Constants.INGREDIENT_COLLECTION_NAME).document(ingredient.getName()).set(ingredient);
        });
    }

    /**
     * Get all ingredients in DB
     *
     * @return A list of {@link Ingredient}
     */
    @SneakyThrows
    public List<Ingredient> getAllIngredients() {
        return firestore.collection(Constants.INGREDIENT_COLLECTION_NAME).get().get().toObjects(Ingredient.class);
    }
}
