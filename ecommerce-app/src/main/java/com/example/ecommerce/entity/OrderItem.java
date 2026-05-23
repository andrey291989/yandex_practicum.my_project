package com.example.ecommerce.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("order_items")
public class OrderItem {

    @Id
    private Long id;
    private Long orderId;
    private String title;
    private String description;
    private String imgPath;
    private Long price;
    private Integer count;

    public OrderItem() {}

    public OrderItem(Long orderId, String title, String description, String imgPath, Long price, Integer count) {
        this.orderId = orderId;
        this.title = title;
        this.description = description;
        this.imgPath = imgPath;
        this.price = price;
        this.count = count;
    }

    // Getters
    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImgPath() { return imgPath; }
    public Long getPrice() { return price; }
    public Integer getCount() { return count; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setImgPath(String imgPath) { this.imgPath = imgPath; }
    public void setPrice(Long price) { this.price = price; }
    public void setCount(Integer count) { this.count = count; }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", title='" + title + '\'' +
                ", count=" + count +
                '}';
    }
}