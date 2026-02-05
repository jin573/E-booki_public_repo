package com.be.ebooki.domain;

import com.be.ebooki.enums.HighlightColor;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "highlight")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
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

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
