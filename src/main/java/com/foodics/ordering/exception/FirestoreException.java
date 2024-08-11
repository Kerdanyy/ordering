package com.foodics.ordering.exception;

public class FirestoreException extends RuntimeException {
    public FirestoreException(Exception ex) {
        super(ex);
    }
}
