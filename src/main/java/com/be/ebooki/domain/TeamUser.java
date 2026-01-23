package com.be.ebooki.domain;

import com.be.ebooki.enums.UserColor;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name="team_users")
public class TeamUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_color")
    private UserColor userColor;

    @Builder
    public TeamUser(User user, Team team, UserColor userColor) {
        this.user = user;
        this.team = team;
        this.userColor = userColor;
    }
}
