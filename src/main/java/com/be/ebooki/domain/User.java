package com.be.ebooki.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor
@Entity
@Table(name="user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "email", length = 100)
    private String email;
    @Setter //비밀번호는 변경 가능
    @Column(name = "password", length = 100)
    private String password;

    @Setter //닉네임은 변경 가능
    @Column(name = "nickname", length = 100)
    private String nickname;

    @Setter //프로필은 변경 가능
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
}
