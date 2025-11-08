package com.be.ebooki.enums;

import java.util.Random;

public enum Nickname {
    ;
    public enum Adjective {
        귀여운, 용감한, 졸린, 배고픈, 신비한, 행복한, 똑똑한, 깜찍한;

        public static Adjective random() {
            Adjective[] values = values();
            return values[new Random().nextInt(values.length)];
        }
    }

    public enum Noun {
        고양이, 호랑이, 펭귄, 토끼, 여우, 사자, 돌고래, 거북이, 꾸부기;

        public static Noun random() {
            Noun[] values = values();
            return values[new Random().nextInt(values.length)];
        }
    }
}
