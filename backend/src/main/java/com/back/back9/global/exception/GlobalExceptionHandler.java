package com.back.back9.global.exception;

import com.back.back9.global.error.ErrorCode;
import com.back.back9.global.error.ErrorException;
import com.back.back9.global.problem.ProblemDetails;
import com.back.back9.global.rsData.RsData;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    // 비즈니스 도메인 에러 (ErrorException)
    @ExceptionHandler(ErrorException.class)
    public ResponseEntity<ProblemDetail> handleErrorException(ErrorException ex) {
        ProblemDetail pd = ProblemDetails.of(ex.getErrorCode(), ex.getArgs());
        return ResponseEntity.status(ex.getErrorCode().getStatus()).body(pd);
    }

    // 서비스 계층 에러 (RsData 포맷)
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<RsData<Void>> handleServiceException(ServiceException ex) {
        return ResponseEntity.status(ex.getRsData().statusCode()).body(ex.getRsData());
    }

    // @Valid 바인딩 에러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetails.of(ErrorCode.INVALID_REQUEST);
        pd.setProperty("fieldErrors", ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "rejectedValue", fe.getRejectedValue(),
                        "message", fe.getDefaultMessage()))
                .toList());
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus()).body(pd);
    }

    // 바인딩/제약조건/메시지 파싱 에러
    @ExceptionHandler({
            BindException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ProblemDetail> handleBindException(Exception ex) {
        ProblemDetail pd = ProblemDetails.of(ErrorCode.INVALID_REQUEST, ex.getMessage());
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus()).body(pd);
    }

    // 경로 파라미터 타입 오류를 400으로 반환
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("잘못된 경로 파라미터 형식입니다.");
    }

    // 그 외 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneralException(Exception ex) {
        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
        ProblemDetail pd = ProblemDetails.of(ErrorCode.INTERNAL_ERROR, ex.getMessage());
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus()).body(pd);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetails.of(ErrorCode.INVALID_REQUEST, ex.getMessage());
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus()).body(pd);
    }
}