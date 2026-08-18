package com.cetc28s.minichatjava.utils;

import java.time.Instant;
import java.util.UUID;

public class CommonUtil {

    /**
     * 生成uuid字符串
     * @return uuid字符串
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成毫秒级时间戳
     * @return 毫秒级时间戳
     */
    public static Long generateTimestamp() {
        return Instant.now().toEpochMilli();
    }
}
