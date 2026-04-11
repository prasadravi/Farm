package com.naturalmilk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import com.google.cloud.firestore.annotation.DocumentId;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @DocumentId
    private String id;
    private String userId;
    private java.util.List<OrderItem> items;
    private double total;
    private DeliveryDetails deliveryDetails;
    private String status;
    private long createdAt;
    private long updatedAt;
    
    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public List<OrderItem> getItems() { return items; }
    public double getTotal() { return total; }
    public DeliveryDetails getDeliveryDetails() { return deliveryDetails; }
    public String getStatus() { return status; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public void setTotal(double total) { this.total = total; }
    public void setDeliveryDetails(DeliveryDetails deliveryDetails) { this.deliveryDetails = deliveryDetails; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private String userId;
        private List<OrderItem> items;
        private double total;
        private DeliveryDetails deliveryDetails;
        private String status;
        private long createdAt;
        private long updatedAt;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder items(List<OrderItem> items) {
            this.items = items;
            return this;
        }
        
        public Builder total(double total) {
            this.total = total;
            return this;
        }

        public Builder deliveryDetails(DeliveryDetails deliveryDetails) {
            this.deliveryDetails = deliveryDetails;
            return this;
        }
        
        public Builder status(String status) {
            this.status = status;
            return this;
        }
        
        public Builder createdAt(long createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(long updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public Order build() {
            Order order = new Order();
            order.id = this.id;
            order.userId = this.userId;
            order.items = this.items;
            order.total = this.total;
            order.deliveryDetails = this.deliveryDetails;
            order.status = this.status;
            order.createdAt = this.createdAt;
            order.updatedAt = this.updatedAt;
            return order;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryDetails {
        private String address;
        private String landmark;
        private String pincode;
        private String phone;

        public String getAddress() { return address; }
        public String getLandmark() { return landmark; }
        public String getPincode() { return pincode; }
        public String getPhone() { return phone; }

        public void setAddress(String address) { this.address = address; }
        public void setLandmark(String landmark) { this.landmark = landmark; }
        public void setPincode(String pincode) { this.pincode = pincode; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private String id;
        private String title;
        private int qty;
        private double price;
        
        // Getters
        public String getId() { return id; }
        public String getTitle() { return title; }
        public int getQty() { return qty; }
        public double getPrice() { return price; }
        
        // Setters
        public void setId(String id) { this.id = id; }
        public void setTitle(String title) { this.title = title; }
        public void setQty(int qty) { this.qty = qty; }
        public void setPrice(double price) { this.price = price; }
        
        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String id;
            private String title;
            private int qty;
            private double price;
            
            public Builder id(String id) {
                this.id = id;
                return this;
            }
            
            public Builder title(String title) {
                this.title = title;
                return this;
            }
            
            public Builder qty(int qty) {
                this.qty = qty;
                return this;
            }
            
            public Builder price(double price) {
                this.price = price;
                return this;
            }
            
            public OrderItem build() {
                OrderItem item = new OrderItem();
                item.id = this.id;
                item.title = this.title;
                item.qty = this.qty;
                item.price = this.price;
                return item;
            }
        }
    }
}
