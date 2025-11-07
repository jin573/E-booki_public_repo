package com.be.ebooki.domain;

import com.be.ebooki.enums.UserType;
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
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Builder
    public User(String email, String password, String nickname, String profileImage, UserType userType){
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.userType = userType;
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
    public void updateUserType(UserType userType) { this.userType = userType; }
}
