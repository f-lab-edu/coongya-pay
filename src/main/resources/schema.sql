-- ============================================================
-- Coongya Pay schema
-- This file runs on every Spring Boot startup
-- (spring.sql.init.mode=always). All statements MUST be idempotent.
-- ============================================================

CREATE TABLE IF NOT EXISTS `user` (
    `id`         INT          NOT NULL AUTO_INCREMENT,
    `email`      VARCHAR(255) NOT NULL,
    `password`   VARCHAR(255) NOT NULL COMMENT 'BCrypt 해시값',
    `name`       VARCHAR(100) NOT NULL,
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` DATETIME     NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_email` (`email`)
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
