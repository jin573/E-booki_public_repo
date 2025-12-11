package com.be.ebooki.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name="teams")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "team_name", length = 100)
    private String teamName;

    @Column(name = "book_id")
    private Integer bookId;
    @Builder
    public Team(String teamName, Integer bookId) {
        this.teamName = teamName;
        this.bookId = bookId;
    }
}
