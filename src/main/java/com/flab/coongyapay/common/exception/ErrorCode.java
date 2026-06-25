package com.flab.coongyapay.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    //Validation
    FIELD_REQUIRED("FIELD_REQUIRED", HttpStatus.BAD_REQUEST, "필수 입력값을 입력하지 않았습니다."),
    INVALID_EMAIL_FORMAT("INVALID_EMAIL_FORMAT", HttpStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다."),
    INVALID_PASSWORD_LENGTH("INVALID_PASSWORD_LENGTH", HttpStatus.BAD_REQUEST, "비밀번호는 최소 8자, 최대 32자까지 입력할 수 있습니다."),
    INVALID_PASSWORD_FORMAT("INVALID_PASSWORD_FORMAT", HttpStatus.BAD_REQUEST, "비밀번호는 영문 대/소문자, 숫자, 특수문자를 모두 포함해야 합니다."),
    INVALID_NAME_LENGTH("INVALID_NAME_LENGTH", HttpStatus.BAD_REQUEST, "이름은 최소 1자, 최대 30자까지 입력할 수 있습니다."),
    INVALID_NAME_FORMAT("INVALID_NAME_FORMAT", HttpStatus.BAD_REQUEST, "이름은 한글 또는 영문 대/소문자만 입력할 수 있습니다. 영문에만 공백 입력이 가능합니다."),
    INVALID_TRANSFER_PIN_FORMAT("INVALID_TRANSFER_PIN_FORMAT", HttpStatus.BAD_REQUEST, "송금 비밀번호는 숫자 6자리를 입력해야 합니다."),

    //Duplicate Email
    DUPLICATE_EMAIL("DUPLICATE_EMAIL", HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),

    //Authentication
    LOGIN_FAILED("LOGIN_FAILED", HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호를 확인해주세요."),
    ACCOUNT_LOCKED("ACCOUNT_LOCKED", HttpStatus.UNAUTHORIZED, "로그인 5회 실패로 계정이 잠겼습니다. 30분 후에 다시 시도해주세요."),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "로그인해주세요."),

    //KYC
    KYC_VERIFICATION_FAILED("KYC_VERIFICATION_FAILED", HttpStatus.BAD_REQUEST, "본인 인증에 실패했습니다."),
    KYC_SYSTEM_UNAVAILABLE("KYC_SYSTEM_UNAVAILABLE", HttpStatus.SERVICE_UNAVAILABLE, "KYC 시스템에 일시적인 장애가 발생했습니다."),

    //Bank Account
    INVALID_ACCOUNT_NUMBER_FORMAT("INVALID_ACCOUNT_NUMBER_FORMAT", HttpStatus.BAD_REQUEST, "계좌번호는 7자리 이상, 15자리 이하의 숫자만 입력해야 합니다."),
    DUPLICATE_ACCOUNT("DUPLICATE_ACCOUNT", HttpStatus.BAD_REQUEST, "이미 등록된 계좌입니다."),
    ACCOUNT_COUNT_LIMIT_EXCEEDED("ACCOUNT_COUNT_LIMIT_EXCEEDED", HttpStatus.BAD_REQUEST, "계좌는 최대 10개까지 등록할 수 있습니다."),

    //Internal Server Error
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류입니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    public static Optional<ErrorCode> of(String code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getCode() != null && errorCode.getCode().equals(code)) {
                return Optional.of(errorCode);
            }
        }
        return Optional.empty();
    }
}
