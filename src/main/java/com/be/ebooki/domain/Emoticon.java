package com.be.ebooki.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "emoticon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emoticon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;

    private Integer commentId;

    @Enumerated(EnumType.STRING)
    private EmojiType emoji;

    private LocalDateTime createdAt;
}
