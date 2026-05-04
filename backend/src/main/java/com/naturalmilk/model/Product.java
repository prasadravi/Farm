package com.naturalmilk.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private int quantity;

    @Column
    private Integer quantity250;

    @Column
    private Integer quantity500;

    @Column
    private Integer quantity1000;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private long createdAt;

    @Column(nullable = false)
    private long updatedAt;

    public Product() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Integer getQuantity250() { return quantity250; }
    public void setQuantity250(Integer quantity250) { this.quantity250 = quantity250; }

    public Integer getQuantity500() { return quantity500; }
    public void setQuantity500(Integer quantity500) { this.quantity500 = quantity500; }

    public Integer getQuantity1000() { return quantity1000; }
    public void setQuantity1000(Integer quantity1000) { this.quantity1000 = quantity1000; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
