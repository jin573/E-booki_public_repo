package com.be.ebooki.config;

import com.be.ebooki.config.jwt.JwtTokenProvider;
import com.be.ebooki.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements HandshakeInterceptor {
    private final JwtTokenProvider jwtTokenProvider;
    private final TeamService teamService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        URI uri = request.getURI();
        String query = uri.getQuery();

        String token = getToken(query);
        Integer teamId = getTeamId(query);

        //토큰 유효성 검사
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return false;
        }

        Integer userId = jwtTokenProvider.getUserIdFromToken(token);

        if (!teamService.isMember(userId, teamId)) {
            return false;
        }

        //세션에 저장
        attributes.put("userId", userId);
        attributes.put("teamId", teamId);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }

    private String getToken(String query) {

        if (query == null) return null;

        for (String param : query.split("&")) {
            String[] kv = param.split("=");
            if ("token".equals(kv[0])) {
                return kv[1].startsWith("Bearer ")
                        ? kv[1].substring(7)
                        : kv[1];
            }
        }
        return null;
    }

    private Integer getTeamId(String query) {
        if(query == null){
            throw new IllegalArgumentException("팀 아이디가 필요합니다.");
        }

        for(String param: query.split("&")){
            String[] k = param.split("=");
            if("teamId".equals(k[0])){
                return Integer.parseInt(k[1]);
            }
        }

        throw new IllegalArgumentException("팀 아이디가 존재하지 않습니다.");
    }


}
