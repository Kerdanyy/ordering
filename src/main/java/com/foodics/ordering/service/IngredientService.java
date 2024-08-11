package com.foodics.ordering.service;

import com.foodics.ordering.Constants;
import com.foodics.ordering.exception.FirestoreException;
import com.foodics.ordering.model.AddIngredientRequest;
import com.foodics.ordering.model.Ingredient;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.StreamSupport;

@Service
public class IngredientService {
    @Autowired
    Firestore firestore;

    public void addIngredients(List<AddIngredientRequest> ingredients) {
        ingredients.forEach(addIngredientRequest -> {
            Ingredient ingredient = new Ingredient(addIngredientRequest.getName(), addIngredientRequest.getQuantity());
            firestore.collection(Constants.INGREDIENT_COLLECTION_NAME).document(ingredient.getName()).set(ingredient);
        });
    }

    public List<Ingredient> getAllIngredients() {
        return StreamSupport.stream(firestore.collection(Constants.INGREDIENT_COLLECTION_NAME).listDocuments().spliterator(), false).map(docRef -> {
            try {
                return docRef.get().get().toObject(Ingredient.class);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new FirestoreException(e);
            } catch (ExecutionException e) {
                throw new FirestoreException(e);
            }
        }).toList();
    }
}
