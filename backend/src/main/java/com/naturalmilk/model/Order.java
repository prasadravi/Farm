package com.naturalmilk.model;

import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderItem> items;

    @Column(nullable = false)
    private double total;

    @Transient
    private DeliveryDetails deliveryDetails;

    private String address;
    private String landmark;
    private String pincode;
    private String phone;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "method", column = @Column(name = "payment_method")),
        @AttributeOverride(name = "status", column = @Column(name = "payment_status")),
        @AttributeOverride(name = "paymentRecordId", column = @Column(name = "payment_record_id")),
        @AttributeOverride(name = "razorpayOrderId", column = @Column(name = "razorpay_order_id")),
        @AttributeOverride(name = "razorpayPaymentId", column = @Column(name = "razorpay_payment_id"))
    })
    private PaymentDetails payment;

    private String status;

    @Column(nullable = false)
    private long createdAt;

    @Column(nullable = false)
    private long updatedAt;

    public Order() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public DeliveryDetails getDeliveryDetails() {
        if (deliveryDetails != null) {
            return deliveryDetails;
        }
        if (address == null && landmark == null && pincode == null && phone == null) {
            return null;
        }
        return new DeliveryDetails(address, landmark, pincode, phone);
    }

    public void setDeliveryDetails(DeliveryDetails deliveryDetails) {
        this.deliveryDetails = deliveryDetails;
        if (deliveryDetails != null) {
            this.address = deliveryDetails.getAddress();
            this.landmark = deliveryDetails.getLandmark();
            this.pincode = deliveryDetails.getPincode();
            this.phone = deliveryDetails.getPhone();
        }
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public PaymentDetails getPayment() { return payment; }
    public void setPayment(PaymentDetails payment) { this.payment = payment; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    @Embeddable
    public static class PaymentDetails {
        private String method;
        private String status;
        private String paymentRecordId;
        private String razorpayOrderId;
        private String razorpayPaymentId;

        public PaymentDetails() {}

        public PaymentDetails(String method, String status, String paymentRecordId, String razorpayOrderId, String razorpayPaymentId) {
            this.method = method;
            this.status = status;
            this.paymentRecordId = paymentRecordId;
            this.razorpayOrderId = razorpayOrderId;
            this.razorpayPaymentId = razorpayPaymentId;
        }

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getPaymentRecordId() { return paymentRecordId; }
        public void setPaymentRecordId(String paymentRecordId) { this.paymentRecordId = paymentRecordId; }

        public String getRazorpayOrderId() { return razorpayOrderId; }
        public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

        public String getRazorpayPaymentId() { return razorpayPaymentId; }
        public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }
    }

    public static class DeliveryDetails {
        private String address;
        private String landmark;
        private String pincode;
        private String phone;

        public DeliveryDetails() {}

        public DeliveryDetails(String address, String landmark, String pincode, String phone) {
            this.address = address;
            this.landmark = landmark;
            this.pincode = pincode;
            this.phone = phone;
        }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getLandmark() { return landmark; }
        public void setLandmark(String landmark) { this.landmark = landmark; }

        public String getPincode() { return pincode; }
        public void setPincode(String pincode) { this.pincode = pincode; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    @Embeddable
    public static class OrderItem {
        private String id;
        private String title;
        private int qty;
        private double price;

        public OrderItem() {}

        public OrderItem(String id, String title, int qty, double price) {
            this.id = id;
            this.title = title;
            this.qty = qty;
            this.price = price;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public int getQty() { return qty; }
        public void setQty(int qty) { this.qty = qty; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }
}
