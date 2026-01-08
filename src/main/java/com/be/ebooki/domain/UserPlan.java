package com.be.ebooki.domain;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_plan")
public class UserPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Plan plan;

    @Column(nullable = false)
    private int totalBookCount;

    @Column(nullable = false)
    private int usedBookCount;

    @Enumerated(EnumType.STRING)
    private UserPlanStatus status;

    private LocalDateTime startedAt;
    private LocalDateTime expiredAt;
}
