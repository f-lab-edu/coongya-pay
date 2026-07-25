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
    ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND", HttpStatus.NOT_FOUND, "계좌를 찾을 수 없습니다."),

    //Bank
    BANK_SYSTEM_UNAVAILABLE("BANK_SYSTEM_UNAVAILABLE", HttpStatus.SERVICE_UNAVAILABLE, "은행 시스템에 일시적인 장애가 발생했습니다."),

    //Charge
    INVALID_IDEMPOTENCY_KEY("INVALID_IDEMPOTENCY_KEY", HttpStatus.BAD_REQUEST, "멱등키가 없거나 형식이 올바르지 않습니다."),
    IDEMPOTENCY_KEY_PROCESSING("IDEMPOTENCY_KEY_PROCESSING", HttpStatus.CONFLICT, "이전 요청이 처리 중입니다. 잠시 후 상태를 조회해주세요."),
    IDEMPOTENCY_KEY_CONFLICT("IDEMPOTENCY_KEY_CONFLICT", HttpStatus.UNPROCESSABLE_CONTENT, "동일한 멱등키로 다른 요청은 처리할 수 없습니다."),
    INVALID_CHARGE_AMOUNT("INVALID_CHARGE_AMOUNT", HttpStatus.BAD_REQUEST, "충전 금액은 1원 이상 2,000,000원 이하여야 합니다."),
    INVALID_REMARK_LENGTH("INVALID_REMARK_LENGTH", HttpStatus.BAD_REQUEST, "거래 적요는 최소 1자, 최대 7자까지 입력할 수 있습니다."),
    INVALID_TRANSFER_PIN("INVALID_TRANSFER_PIN", HttpStatus.UNAUTHORIZED, "송금 비밀번호를 확인해주세요."),
    TRANSFER_PIN_LOCKED("TRANSFER_PIN_LOCKED", HttpStatus.LOCKED, "송금 비밀번호 5회 오류로 잠겼습니다."),
    INVALID_SENDER_ACCOUNT("INVALID_SENDER_ACCOUNT", HttpStatus.BAD_REQUEST, "출금 계좌가 존재하지 않거나 거래할 수 없는 상태입니다."),
    BANK_MAINTENANCE("BANK_MAINTENANCE", HttpStatus.BAD_REQUEST, "은행 점검 시간(00:00~00:30)에는 송금할 수 없습니다."),
    WALLET_BALANCE_LIMIT_EXCEEDED("WALLET_BALANCE_LIMIT_EXCEEDED", HttpStatus.BAD_REQUEST, "페이머니 보유 한도(2,000,000원)를 초과할 수 없습니다."),

    //Transaction
    TRANSACTION_NOT_FOUND("TRANSACTION_NOT_FOUND", HttpStatus.NOT_FOUND, "거래를 찾을 수 없습니다."),

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
