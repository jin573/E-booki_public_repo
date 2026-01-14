package com.be.ebooki.dto;

import com.be.ebooki.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StompResponse<T>{
    private MessageType type;
    private Integer teamId;
    private Integer bookId;
    private T data;

}
