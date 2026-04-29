package com.flab.coongyapay.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Validation
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다."),
    INVALID_PASSWORD_LENGTH(HttpStatus.BAD_REQUEST, "비밀번호는 최소 8자, 최대 32자까지 입력할 수 있습니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "비밀번호는 영문 대/소문자, 숫자, 특수문자를 모두 포함해야 합니다."),
    INVALID_NAME_LENGTH(HttpStatus.BAD_REQUEST, "이름은 최소 1자, 최대 7자까지 입력할 수 있습니다."),
    INVALID_NAME_FORMAT(HttpStatus.BAD_REQUEST, "이름은 한글 또는 영문 대/소문자만 입력할 수 있습니다."),
    INVALID_TRANSFER_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "송금 비밀번호는 숫자 6자리를 입력해야 합니다."),

    // Duplicate Email
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),

    // Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류입니다."),

    // KYC
    KYC_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "본인 인증에 실패했습니다."),
    KYC_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "KYC 서비스에 일시적인 장애가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
