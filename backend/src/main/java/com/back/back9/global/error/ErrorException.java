package com.back.back9.global.error;

import lombok.Getter;

@Getter
public class ErrorException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object[] args;

    public ErrorException(ErrorCode errorCode, Object... args) {
        super(errorCode.name());
        this.errorCode = errorCode;
        this.args = args;
    }

}
