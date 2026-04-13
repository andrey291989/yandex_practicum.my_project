package com.example.ecommerce.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String imgPath;
    private Long price;
    private Integer count;

    public OrderItem() {}

    public OrderItem(Item item, Integer count) {
        this.title = item.getTitle();
        this.description = item.getDescription();
        this.imgPath = item.getImgPath();
        this.price = item.getPrice();
        this.count = count;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImgPath() { return imgPath; }
    public void setImgPath(String imgPath) { this.imgPath = imgPath; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}