package com.be.ebooki.dto;

import com.be.ebooki.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserResponse {


    //공통 응답
    @Getter
    @Builder
    public static class UserResponseDTO<T>{
        private int statusCode;
        private String message;
        private T data;

    }

    //유저 응답
    @Getter
    @Builder
    public static class UserInfoDTO {
        private Integer id;
        private String email;
        private String nickname;
        private String profileImage;
        public static UserInfoDTO from(User user) {
            return UserInfoDTO.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .profileImage(user.getProfileImage())
                    .build();
        }

    }

    //유저 로그인 응답
    @Getter
    @Builder
    public static class UserLoginDTO{
        private String accessToken;
        private String refreshToken;
        private UserInfoDTO userInfoDTO;

    }
}
