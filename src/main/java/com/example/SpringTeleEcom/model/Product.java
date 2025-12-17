package com.example.SpringTeleEcom.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    private String brand;

    @Column(nullable = false)
    private BigDecimal price;

    private String category;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date releaseDate;

    @Column(nullable = false)
    private boolean productAvailable;

    @Column(nullable = false)
    private int stockQuantity;

    private String imageName;
    private String imageType;

    @Lob
    @JsonIgnore // Don't serialize in list view - use /product/{id}/image endpoint instead
    private byte[] imageData;

}
