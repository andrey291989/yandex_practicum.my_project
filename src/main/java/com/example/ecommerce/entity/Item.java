package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "img_path")
    private String imgPath;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer count;

    public Item() {
    }

    public Item(String title, String description, String imgPath, Long price, Integer count) {
        this.title = title;
        this.description = description;
        this.imgPath = imgPath;
        this.price = price;
        this.count = count;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImgPath() { return imgPath; }
    public Long getPrice() { return price; }
    public Integer getCount() { return count; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setImgPath(String imgPath) { this.imgPath = imgPath; }
    public void setPrice(Long price) { this.price = price; }
    public void setCount(Integer count) { this.count = count; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}