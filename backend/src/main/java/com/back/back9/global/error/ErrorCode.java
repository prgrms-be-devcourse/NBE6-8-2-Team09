package com.back.back9.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 400
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "400-INVALID-REQUEST", "잘못된 요청입니다."),
    // 401
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "401-UNAUTHORIZED", "인증이 필요합니다."),
    // 403
    FORBIDDEN(HttpStatus.FORBIDDEN, "403-FORBIDDEN", "접근 권한이 없습니다."),
    // 404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "404-USER-NOT-FOUND", "사용자를 찾을 수 없습니다. id=%s"),
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "404-WALLET-NOT-FOUND", "지갑을 찾을 수 없습니다. id=%s"),
    COIN_NOT_FOUND(HttpStatus.NOT_FOUND, "404-COIN-NOT-FOUND", "코인을 찾을 수 없습니다. id=%s"),
    // 409
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "409-DUPLICATED-EMAIL", "이미 존재하는 이메일입니다. email=%s"),
    // 500
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500-INTERNAL-ERROR", "서버 내부 오류입니다.");

    private final HttpStatus status;
    private final String code;
    private final String defaultDetail; // detail 포맷(가변 인자 치환용)

    ErrorCode(HttpStatus status, String code, String defaultDetail) {
        this.status = status;
        this.code = code;
        this.defaultDetail = defaultDetail;
    }

}