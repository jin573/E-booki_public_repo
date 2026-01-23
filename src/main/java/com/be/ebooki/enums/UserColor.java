package com.be.ebooki.enums;
import java.util.*;
import java.util.stream.Collectors;

public enum UserColor {
    PINK, GREEN, BLUE, YELLOW;

    private static final List<UserColor> VALUES = List.of(values()); //기준
    private static final Random RANDOM = new Random();

    public static UserColor randomColor(Set<UserColor> excludedColor){
        List<UserColor> availableColor = VALUES.stream()
                .filter(color -> !excludedColor.contains(color))
                .collect(Collectors.toList()); //사용된 컬러 제외
        if(availableColor.isEmpty()){
            throw new IllegalArgumentException("사용 가능한 컬러가 없습니다.");
        }

        return availableColor.get(RANDOM.nextInt(availableColor.size())); //랜덤으로 반환하기
    }
}
