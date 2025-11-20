package com.be.ebooki.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceTest {

    @Test
    void testGetCurrentUserId_Success(){
        //fake id 준비
        Integer fakeUserId = 42;

        //securityContext 설정
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                fakeUserId,
                null,
                null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        //method
        Integer currentUserId = UserService.getCurrentUserId();

        //검증
        assertEquals(fakeUserId, currentUserId);

        //초기화
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUserId_NoAuthentication(){
        // SecurityContext 초기화 (인증 정보 없음)
        SecurityContextHolder.clearContext();

        // 인증 정보 없을 때 예외 발생 확인
        assertThrows(IllegalStateException.class, UserService::getCurrentUserId);
    }

    @Test
    void testGetCurrentUserId_InvalidPrincipal() {
        // principal이 Integer가 아닌 경우
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "not-an-integer",
                        null,
                        null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 예외 발생 확인
        assertThrows(IllegalStateException.class, UserService::getCurrentUserId);

        // SecurityContext 초기화
        SecurityContextHolder.clearContext();
    }

}
