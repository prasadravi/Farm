package com.naturalmilk.model.payment;

import com.naturalmilk.model.Order;
import java.util.List;

public class CreatePaymentOrderRequest {
    private List<Order.OrderItem> items;
    private Double total;
    private Order.DeliveryDetails deliveryDetails;

    public CreatePaymentOrderRequest() {
    }

    public CreatePaymentOrderRequest(List<Order.OrderItem> items, Double total, Order.DeliveryDetails deliveryDetails) {
        this.items = items;
        this.total = total;
        this.deliveryDetails = deliveryDetails;
    }

    public List<Order.OrderItem> getItems() {
        return items;
    }

    public void setItems(List<Order.OrderItem> items) {
        this.items = items;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Order.DeliveryDetails getDeliveryDetails() {
        return deliveryDetails;
    }

    public void setDeliveryDetails(Order.DeliveryDetails deliveryDetails) {
        this.deliveryDetails = deliveryDetails;
    }
}
