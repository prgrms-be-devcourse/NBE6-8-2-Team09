package com.back.back9.global.rsData;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public record RsData<T>(
        @JsonProperty("status")
        String status,
        @JsonProperty("code")
        int code,
        @JsonProperty("message")
        String message,
        @JsonProperty("result")
        T result,
        @JsonIgnore
        String resultCode,
        @JsonIgnore
        int statusCode,
        @JsonIgnore
        String msg,
        @JsonIgnore
        T data
) {
    public RsData(String resultCode, String msg) {
        this(resultCode, msg, null);
    }

    public RsData(String resultCode, String msg, T data) {
        this(
                resultCode.startsWith("2") ? "success" : "fail",
                Integer.parseInt(resultCode.split("-", 2)[0]),
                msg,
                data,
                resultCode,
                Integer.parseInt(resultCode.split("-", 2)[0]),
                msg,
                data
        );
    }
}