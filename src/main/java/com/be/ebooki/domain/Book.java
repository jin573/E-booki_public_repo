package com.be.ebooki.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="title", length = 100)
    private String title;

    @Column(name="author", length = 100)
    private String author;

    @Column(name="publisher", length = 100)
    private String publisher;

    @Column(name="price")
    private Integer price;

    @Column(name="book_image", length = 500)
    private String bookImage;

    @Column(name="rating")
    private Double rating;



}
