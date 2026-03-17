package com.be.ebooki.dto;

import com.be.ebooki.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class UserRequest {

    //유저 요청
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserSignupDTO{
        @NotBlank
        @Email
        private String email;
        @NotBlank
        @Size(min = 6)
        private String password;
        private String profileImage;

        public User toEntity(){
            return User.builder()
                    .email(this.email)
                    .password(this.password)
                    .profileImage(this.profileImage)
                    .build();
        }
    }

    //유저 로그인
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserLoginDTO{
        @NotBlank
        @Email()
        private String email;
        @NotBlank
        @Size(min = 6)
        private String password;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenReissueDTO {
        private String accessToken;
        private String refreshToken;
    }

    //프로필 이미지 변경
    @Getter
    public static class UpdateProfileImageDTO {
        private String profileImage;
    }
}
