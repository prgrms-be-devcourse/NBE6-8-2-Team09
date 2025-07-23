package com.back.back9.global.rsData;

import com.back.back9.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

public record RsData<T>(
        String resultCode,
        @JsonIgnore
        int statusCode,
        String msg,
        T data
) {
    public RsData(String resultCode, String msg) {
        this(resultCode, msg, null);
    }

    public RsData(String resultCode, String msg, T data) {
        this(resultCode, Integer.parseInt(resultCode.split("-", 2)[0]), msg, data);
    }

    public static RsData<User> of(String resultCode, String msg) {
        return new RsData<>(resultCode, msg, null);
    }
}
