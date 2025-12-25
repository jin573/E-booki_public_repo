package com.be.ebooki.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
//provider를 통해 검증 후 객체 설정
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        //회원가입과 로그인은 누구나 접근
        if (path.contains("/auth/signup") || path.contains("/auth/login") || path.contains("/auth/reissue")
                || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") || path.startsWith("/swagger-ui.html")|| path.startsWith("/h2-console")
                || path.contains("/auth/login/kakao")
                || path.equals("/api/teams/invite")) {
            filterChain.doFilter(request, response);
            return;
        }
        //그 외 경로 인증
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            //인증 실패시 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        //헤더에서 토큰 추출하기
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            //유효성 검사
            if (jwtTokenProvider.validateToken(token)) {
                //String email = jwtTokenProvider.getEmailFromToken(token);
                Integer userId = jwtTokenProvider.getUserIdFromToken(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, AuthorityUtils.NO_AUTHORITIES);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

}
