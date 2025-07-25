package com.back.back9.global.problem;

import com.back.back9.global.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;

public final class ProblemDetails {

    private ProblemDetails() {}

    public static ProblemDetail of(ErrorCode code, Object... args) {
        String detail = args == null || args.length == 0
                ? code.getDefaultDetail()
                : String.format(code.getDefaultDetail(), args);


        ProblemDetail pd = ProblemDetail.forStatus(code.getStatus());
        pd.setTitle(code.name());
        pd.setType(URI.create("about:blank"));
        pd.setProperty("code", code.getCode());
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;

    }

    public static ProblemDetail of(HttpStatus status, String title,
                                   String detail,
                                   String code,
                                   Map<String, Object> properties) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);

        if(code != null) pd.setProperty("code", code);
        if(properties != null) {
            properties.forEach(pd::setProperty);
        }
        pd.setProperty("timestamp", OffsetDateTime.now());

        return pd;
}
}