package com.be.ebooki.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebsocketHandler extends TextWebSocketHandler {

    private final ConcurrentHashMap<String, WebSocketSession> clients = new ConcurrentHashMap<String, WebSocketSession>();


    //서버 접속 시 client 저장
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        clients.put(session.getId(), session);
        System.out.println("[SOCKET LOG] CONNECT: " + session.getId() + " | total = " + clients.size());
    }

    //서버에서 나갈 시 제거
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        clients.remove(session.getId());
        System.out.println("[SOCKET LOG] DISCONNECT: " + session.getId() + " | total = " + clients.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String id = session.getId();//접속 소켓

        clients.forEach((key, client) ->{
                if(!key.equals(id) && client.isOpen()){//본인을 제외한 곳에 && 세션이 열린 곳에만 메세지 전달
                    try{
                        client.sendMessage(message);
                        System.out.println("[SOCKET LOG] MSG from: " + session.getId() + " : " + message.getPayload());
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
        });
    }


}
