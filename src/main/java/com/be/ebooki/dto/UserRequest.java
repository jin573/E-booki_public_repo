package com.be.ebooki.dto;

import com.be.ebooki.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserRequest {

    //유저 요청
    @Getter
    @Builder
    public static class UserSignupDTO{
        private String email;
        private String password;
        private String nickname;
        private String profileImage;

        public User toEntity(){
            return User.builder()
                    .email(this.email)
                    .password(this.password)
                    .nickname(this.nickname)
                    .profileImage(this.profileImage)
                    .build();
        }
    }

    //유저 로그인
    @Getter
    @Builder
    public static class UserLoginDTO{
        private String email;
        private String password;
    }
}
