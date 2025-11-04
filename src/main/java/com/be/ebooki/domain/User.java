package com.be.ebooki.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor
@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "password", length = 100)
    private String password;


    @Column(name = "nickname", length = 100)
    private String nickname;


    @Column(name = "profile_image", length = 500)
    private String profileImage;

    //소셜 로그인 및 소셜 로그인 종류 추가

    @Builder
    public User(String email, String password, String nickname, String profileImage){
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    public void updatePassword(String password){
        this.password = password;
    }

    public void updateNickname(String nickname){
        this.nickname = nickname;
    }

    public void updateProfileImage(String profileImage){
        this.profileImage = profileImage;
    }
}
