package com.back.back9.global.exception;

import com.back.back9.global.error.ErrorCode;
import com.back.back9.global.error.ErrorException;
import com.back.back9.global.problem.ProblemDetails;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ErrorException.class)
    public ResponseEntity<ProblemDetail> handleErrorException(ErrorException ex) {
        ProblemDetail pd = ProblemDetails.of(ex.getErrorCode(), ex.getArgs());
        return ResponseEntity
                .status(ex.getErrorCode().getStatus()).body(pd);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        var pd = ProblemDetails.of(ErrorCode.INVALID_REQUEST);
        pd.setProperty("fieldErrors", ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "rejectedValue", fe.getRejectedValue(),
                        "message", fe.getDefaultMessage()))
                .toList());

        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getStatus())
                .body(pd);
    }

    @ExceptionHandler({BindException.class, ConstraintViolationException.class,
    HttpMessageNotReadableException.class})
    public ResponseEntity<ProblemDetail> handleBindException(Exception ex) {
        var pd = ProblemDetails.of(ErrorCode.INVALID_REQUEST, ex.getMessage());
        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getStatus())
                .body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneralException(Exception ex) {
        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
        var pd = ProblemDetails.of(ErrorCode.INTERNAL_ERROR, ex.getMessage());
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getStatus())
                .body(pd);
    }


}
