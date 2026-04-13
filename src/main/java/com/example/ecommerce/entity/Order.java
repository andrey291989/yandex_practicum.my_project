package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)  // Изменено с EAGER на LAZY
    @JoinColumn(name = "order_id")
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "total_sum")
    private Long totalSum;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Order() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public List<OrderItem> getItems() { return items; }
    public Long getTotalSum() { return totalSum; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public void setTotalSum(Long totalSum) { this.totalSum = totalSum; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}