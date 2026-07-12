-- ============================================================
-- Coongya Pay schema
-- This file runs on every Spring Boot startup
-- (spring.sql.init.mode=always). All statements MUST be idempotent.
-- ============================================================

CREATE TABLE IF NOT EXISTS `user` (
    `id`         INT          NOT NULL AUTO_INCREMENT,
    `email`      VARCHAR(255) NOT NULL,
    `name`       VARCHAR(100) NOT NULL,
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` DATETIME     NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_email` (`email`)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user_password` (
    `id`                 INT          NOT NULL AUTO_INCREMENT,
    `user_id`            INT          NOT NULL,
    `password`           VARCHAR(255) NOT NULL COMMENT 'BCrypt 해시값',
    `failed_login_count` INT          NOT NULL DEFAULT 0,
    `locked_until`       DATETIME     NULL,
    `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_password_user_id` (`user_id`)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `wallet` (
    `id`         INT           NOT NULL AUTO_INCREMENT,
    `user_id`    INT           NOT NULL,
    `balance`    DECIMAL(15,0) NOT NULL DEFAULT 0 COMMENT 'KRW (정수, 하위 단위 없음)',
    `version`    BIGINT        NOT NULL DEFAULT 0 COMMENT 'transaction_entry의 최신 wallet_sequence',
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wallet_user_id` (`user_id`)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user_transfer_pin` (
    `id`                        INT          NOT NULL AUTO_INCREMENT,
    `user_id`                   INT          NOT NULL,
    `transfer_pin`              VARCHAR(255) NOT NULL COMMENT 'BCrypt 해시값',
    `failed_transfer_pin_count` INT          NOT NULL DEFAULT 0,
    `locked_until`              DATETIME     NULL,
    `created_at`                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_transfer_pin_user_id` (`user_id`)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user_login_history` (
    `id`	                INT	         NOT NULL AUTO_INCREMENT,
    `user_id`	            INT	         NOT NULL,
    `success`	            BOOLEAN	     NOT NULL,
    `failure_reason`	    VARCHAR(100) NULL,
    `login_at`	            DATETIME	 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ip_address`	        VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX idx_user_id (`user_id`)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `bank_account` (
    `id`                  INT          NOT NULL AUTO_INCREMENT,
    `user_id`             INT          NOT NULL,
    `bank_code`           VARCHAR(100) NOT NULL COMMENT '금융결제원 은행코드',
    `account_number`      VARCHAR(100) NOT NULL,
    `account_holder_name` VARCHAR(100) NOT NULL,
    `registered_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at`          DATETIME     NULL,
    `active_flag`         TINYINT GENERATED ALWAYS AS (IF(`deleted_at` IS NULL, 1, NULL)) VIRTUAL,
    PRIMARY KEY (`id`),
    INDEX idx_bank_account_user_id (`user_id`),
    UNIQUE KEY uk_active_bank_account (`user_id`, `bank_code`, `account_number`, `active_flag`)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `transaction` (
    `id`                     BIGINT        NOT NULL AUTO_INCREMENT,
    `wallet_id`              INT           NOT NULL,
    `transaction_type`       VARCHAR(20)   NOT NULL COMMENT 'CHARGE,WITHDRAW,TRANSFER,COMPENSATION',
    `parent_transaction_id`  BIGINT        NULL COMMENT 'COMPENSATION 거래의 원거래 ID',
    `amount`                 DECIMAL(15,0) NOT NULL,
    `status`                 VARCHAR(20)   NOT NULL,
    `remark`                 VARCHAR(255)  NULL,
    `failure_reason`         VARCHAR(40)   NULL,
    `external_idempotency_key` VARCHAR(100) NULL COMMENT '은행 전송용 멱등키',
    `lease_token`            BIGINT        NOT NULL DEFAULT 0 COMMENT 'fencing 카운터',
    `lease_expires_at`       DATETIME      NULL,
    `next_retry_at`          DATETIME      NULL,
    `retry_count`            INT           NOT NULL DEFAULT 0,
    `max_retries`            INT           NOT NULL DEFAULT 3,
    `requery_count`          INT           NOT NULL DEFAULT 0 COMMENT 'UNKNOWN 재조회 횟수',
    `first_attempt_at`       DATETIME      NULL,
    `created_at`             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `completed_at`           DATETIME      NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_compensation_parent` (`parent_transaction_id`),
    INDEX `idx_tx_wallet_id` (`wallet_id`),
    INDEX `idx_tx_claim` (`status`, `next_retry_at`)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `transaction_entry` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT,
    `transaction_id`  BIGINT        NOT NULL,
    `wallet_id`       INT           NOT NULL,
    `entry_type`      VARCHAR(20)   NOT NULL COMMENT 'CREDIT,DEBIT',
    `amount`          DECIMAL(15,0) NOT NULL,
    `balance_after`   DECIMAL(15,0) NOT NULL,
    `wallet_sequence` BIGINT        NOT NULL,
    `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_entry_tx_type` (`transaction_id`, `entry_type`),
    UNIQUE KEY `uk_entry_wallet_seq` (`wallet_id`, `wallet_sequence`),
    INDEX `idx_entry_wallet` (`wallet_id`)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;