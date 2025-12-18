package com.be.ebooki.config;

import com.be.ebooki.config.jwt.JwtTokenProvider;
import com.be.ebooki.dto.WebSocketResponse;
import com.be.ebooki.service.TeamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WebsocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    //teamId로 session을 묶음
    private final ConcurrentHashMap<Integer, Set<WebSocketSession>> teamSessions = new ConcurrentHashMap<>();
    private final JwtTokenProvider jwtTokenProvider;

    private final TeamService teamService;

    //서버 접속 시 client 저장
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Integer teamId = getTeamId(session);
        String token = getToken(session);
        
        //JWT 토큰 검증
        if(token == null || !jwtTokenProvider.validateToken(token)){
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("JWT invalid in SOCKET"));
            return;
        }
        
        Integer userId = jwtTokenProvider.getUserIdFromToken(token);

        //세션에 저장
        session.getAttributes().put("userId", userId);
        session.getAttributes().put("teamId", teamId);

        //팀 멤버인지 확인
        if(!teamService.isMember(userId, teamId)){
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Not Team Member in SOCKET"));
            return;
        }
        
        //세션에 없을 경우 추가
        teamSessions
                .computeIfAbsent(teamId, k->ConcurrentHashMap.newKeySet())
                .add(session);

        System.out.println("[SOCKET LOG] CONNECT teamId = " + teamId
                + " session = " + session.getId()
                + " size = " + teamSessions.get(teamId).size());
    }

    private String getToken(WebSocketSession session) {
        String query = session.getUri().getQuery();
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

    //서버에서 나갈 시 제거
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        teamSessions.forEach((teamId, sessions) -> {
            if(sessions.remove(session)){
                System.out.println("[SOCKET LOG] DISCONNECT teamId = " + teamId
                        + " session = " + session.getId());
                if(sessions.isEmpty()){ //세션에서 제거 후 맵에서 제거
                    teamSessions.remove(teamId);
                }
            }
        });

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //JSON -> Object
        WebSocketResponse webSocketResponse = objectMapper.readValue(message.getPayload(), WebSocketResponse.class);

        System.out.println("[SOCKET LOG] type = " + webSocketResponse.getType() + ", teamId = " + webSocketResponse.getTeamId());

        //추후 분기
        switch(webSocketResponse.getType()){
            case COMMENT -> broadcast(session, webSocketResponse);
            case EMOJI -> broadcast(session, webSocketResponse);
            case HIGHLIGHT -> broadcast(session, webSocketResponse);
            case PAGE_SYNC -> broadcast(session, webSocketResponse);
        }
    }

    private void broadcast(WebSocketSession sender, WebSocketResponse webSocketResponse){
        Integer teamId = (Integer) sender.getAttributes().get("teamId");
        Set<WebSocketSession> sessions = teamSessions.get(teamId);

        if(sessions == null) return;
        //같은 teamId 내 세션에게 메세지 전송하기
        sessions.forEach(session -> {
            if(session.isOpen() && session != sender){
                try{
                    session.sendMessage(new TextMessage(writeJson(webSocketResponse)));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        });
    }

    //teamId 파싱함수
    private Integer getTeamId(WebSocketSession session){
        String query = session.getUri().getQuery();
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

    //JSON -> Object
    private String writeJson(Object obj) {
        try{
            return objectMapper.writeValueAsString(obj);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
