package com.naturalmilk.model.payment;

import java.util.List;

import com.naturalmilk.model.Order;

public class CreatePaymentOrderRequest {
    private int amount;
    private String currency;
    private String receipt;
    private List<Order.OrderItem> items;
    private Double total;
    private Order.DeliveryDetails deliveryDetails;

    public CreatePaymentOrderRequest() {}

    public CreatePaymentOrderRequest(int amount, String currency, String receipt) {
        this.amount = amount;
        this.currency = currency;
        this.receipt = receipt;
    }

    public CreatePaymentOrderRequest(List<Order.OrderItem> items, Double total, Order.DeliveryDetails deliveryDetails) {
        this.items = items;
        this.total = total;
        this.deliveryDetails = deliveryDetails;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    public List<Order.OrderItem> getItems() { return items; }
    public void setItems(List<Order.OrderItem> items) { this.items = items; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public Order.DeliveryDetails getDeliveryDetails() { return deliveryDetails; }
    public void setDeliveryDetails(Order.DeliveryDetails deliveryDetails) { this.deliveryDetails = deliveryDetails; }
}
