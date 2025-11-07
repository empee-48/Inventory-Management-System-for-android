package com.brocode.utils;

public class ProductOutOfStockException extends RuntimeException {
    public ProductOutOfStockException(String productName, double requestedAmount, double currentStock) {
        super(String.format(
                "Product '%s' is out of stock. Requested: %.2f, Available: %.2f",
                productName, requestedAmount, currentStock
        ));
    }

    public ProductOutOfStockException(String message) {
        super(message);
    }
}