package com.back.back9.global.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RedisDTO {
    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
    private long timestamp;
}