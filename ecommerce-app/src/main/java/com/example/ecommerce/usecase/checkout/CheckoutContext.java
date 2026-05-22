package com.example.ecommerce.usecase.checkout;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.entity.Order;

import java.util.List;
import java.util.Map;

/**
 * Контекст оформления заказа, содержащий промежуточные данные между шагами
 */
public class CheckoutContext {

    private final Map<Long, Integer> cartItems;
    private List<Item> validatedItems;
    private Order order;
    private Long totalSum;

    public CheckoutContext(Map<Long, Integer> cartItems) {
        this.cartItems = cartItems;
    }

    // Getters and Setters

    public Map<Long, Integer> getCartItems() {
        return cartItems;
    }

    public List<Item> getValidatedItems() {
        return validatedItems;
    }

    public void setValidatedItems(List<Item> validatedItems) {
        this.validatedItems = validatedItems;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Long getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(Long totalSum) {
        this.totalSum = totalSum;
    }
}