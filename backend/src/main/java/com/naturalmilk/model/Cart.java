package com.naturalmilk.model;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String userId;

    @ElementCollection
    @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "cart_id"))
    private List<CartItem> items;

    @Column(nullable = false)
    private long updatedAt;

    public Cart() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    @Embeddable
    public static class CartItem {
        @Column(name = "product_id")
        private String id;
        private String title;
        private String baseKey;
        private String baseTitle;
        private String unit;
        private int sizeValue;
        private double price;
        private int qty;
        private String img;

        public CartItem() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getBaseKey() { return baseKey; }
        public void setBaseKey(String baseKey) { this.baseKey = baseKey; }

        public String getBaseTitle() { return baseTitle; }
        public void setBaseTitle(String baseTitle) { this.baseTitle = baseTitle; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public int getSizeValue() { return sizeValue; }
        public void setSizeValue(int sizeValue) { this.sizeValue = sizeValue; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public int getQty() { return qty; }
        public void setQty(int qty) { this.qty = qty; }

        public String getImg() { return img; }
        public void setImg(String img) { this.img = img; }
    }
}
