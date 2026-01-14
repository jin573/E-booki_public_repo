package com.be.ebooki.config;

import com.be.ebooki.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/*
* 해당 config가 하는 일
* 이 클라이언트가 해당 topic을 구독할 자격이 있는가?
* == 이 작업 공간의 변경 사항을 보는 게 허용 되는가?
* == 같은 team 멤버에게만 뿌린다.
*  */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

    private final TeamService teamService;

    //해당 team에 가입한 팀원들만 접속할 수 있도록
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            String destination = accessor.getDestination();
            Integer userId = (Integer) accessor.getSessionAttributes().get("userId");

            Integer teamId = extractTeamId(destination);

            log.info("SUBSCRIBE user={} dest={}", userId, destination);

            if (!teamService.validateMember(teamId, userId)) {
                throw new IllegalArgumentException("팀원이 아닙니다.");
            }
        }

        return message;
    }

    private Integer extractTeamId(String destination) {
        // /topic/teams/{teamId}/books/{bookId}
        String[] parts = destination.split("/");
        return Integer.parseInt(parts[3]);
    }
}
