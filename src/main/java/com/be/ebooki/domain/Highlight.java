package com.be.ebooki.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "highlight")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Highlight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;

    private Integer teamId;

    private Integer bookId;

    @Enumerated(EnumType.STRING)
    private HighlightColor color;

    private Integer spineIndex;

    @Column(length = 500)
    private String cfi;

    @Column(length = 1000)
    private String text;

    private LocalDateTime createdAt;
}
