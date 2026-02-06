package com.be.ebooki.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "user_book_progress")
public class UserBookProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    private Integer percentage;

    @Column(length = 500)
    private String cfi;

    private Integer spineIndex;

    private LocalDateTime updatedAt;

    // 개인 평점 컬럼 추가 -> 얘기하기!
    private Integer rating;

    @Builder
    public UserBookProgress(User user,
                            Book book,
                            Integer percentage,
                            String cfi,
                            Integer spineIndex,
                            LocalDateTime updatedAt,
                            Integer rating) {
        this.user = user;
        this.book = book;
        this.percentage = percentage;
        this.cfi = cfi;
        this.spineIndex = spineIndex;
        this.updatedAt = updatedAt;
        this.rating = rating;
    }

    public void updateProgress(Integer spineIndex, String cfi) {
        this.spineIndex = spineIndex;
        this.cfi = cfi;
        this.updatedAt = LocalDateTime.now();
    }

    //미리 업데이트용
    public void updateRating(Integer rating) {
        this.rating = rating;
    }
}
