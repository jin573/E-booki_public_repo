package com.be.ebooki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoResponse {

    @Getter
    public static class OAuthToken {
        private String access_token;
        private String token_type;
        private String refresh_token;
        private int expires_in;
        private String scope;
        private int refresh_token_expires_in;
    }

    @Getter
    public static class KakaoProfile {
        private Long id;

        private Properties properties;

        @JsonProperty("kakao_account")
        private KakaoAccount kakaoAccount;

        @Getter
        public static class Properties {
            private String nickname;
        }

        @Getter
        public static class KakaoAccount {
            private String email;
            private Boolean is_email_verified;
            private Boolean profile_nickname_needs_agreement;
            private Boolean is_email_valid;
            private Profile profile;

            @Getter
            public static class Profile {
                private String nickname;
            }
        }
    }
}
