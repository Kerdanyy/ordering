package com.foodics.ordering.model;

public enum IngredientEnum {
    BEEF("Beef", 150), CHEESE("Cheese", 30), ONION("Onion", 20);

    private final String name;
    private final int quantity;

    IngredientEnum(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public static int getQuantity(String name) {
        for (IngredientEnum ingredient : values()) {
            if (ingredient.getName().equalsIgnoreCase(name)) {
                return ingredient.getQuantity();
            }
        }
        throw new IllegalArgumentException("No ingredient found with name: " + name);
    }
}
