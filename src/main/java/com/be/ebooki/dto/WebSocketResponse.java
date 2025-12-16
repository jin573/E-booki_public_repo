package com.be.ebooki.dto;

import com.be.ebooki.enums.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WebSocketResponse {

    private MessageType type;
    private Integer teamId;
    private Object payload;

}
