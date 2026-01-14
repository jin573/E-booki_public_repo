package com.be.ebooki.domain;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "plan")
public class Plan {

    @Id @GeneratedValue
    private Long id;

    private String name;           // "작심삼일 요금제"
    private int totalBookCount;    // 3, 6, 9
    private int price;             // 9900, 15000, 21000
    }
