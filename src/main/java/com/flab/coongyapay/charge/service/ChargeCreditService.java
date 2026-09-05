package com.flab.coongyapay.charge.service;

import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import com.flab.coongyapay.transaction.domain.Transaction;
import com.flab.coongyapay.transaction.domain.TransactionEntry;
import com.flab.coongyapay.transaction.enums.TransactionStatus;
import com.flab.coongyapay.transaction.repository.TransactionEntryRepository;
import com.flab.coongyapay.transaction.repository.TransactionRepository;
import com.flab.coongyapay.wallet.domain.Wallet;
import com.flab.coongyapay.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 지갑 크레딧(로컬 원장 기록). 은행 출금이 확정된(WITHDRAWN/DEPOSITING) 거래를 지갑에 반영한다.
 * wallet FOR UPDATE → 한도 재검증 → 원장 CREDIT append → balance/version UPDATE → COMPLETED 를
 * 하나의 트랜잭션에서 원자적으로 수행한다.
 *
 * 멱등성: (transaction_id, entry_type) UNIQUE + check-then-insert. 이미 CREDIT이 있으면
 * 이중 입금하지 않고 성공으로 간주한다(재시도/중복 처리 안전).
 */
@Service
@RequiredArgsConstructor
public class ChargeCreditService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionEntryRepository transactionEntryRepository;
    private final Clock clock;

    @Transactional
    public void credit(Long transactionId, long leaseToken) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));

        // 이미 크레딧된 경우 멱등 성공: 상태만 COMPLETED로 마감 시도(펜싱)
        if (transactionEntryRepository.existsCredit(transactionId)) {
            transactionRepository.updateStatusFenced(transactionId, TransactionStatus.DEPOSITING,
                    TransactionStatus.COMPLETED, null, LocalDateTime.now(clock), leaseToken);
            return;
        }

        // 모든 잔액 기록자는 wallet 행을 먼저 락한다.
        Wallet wallet = walletRepository.findByIdForUpdate(transaction.getWalletId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));

        // 크레딧 시점 한도 재검증 (락 보유). 초과 시 보상 대상(3D). 3A에서는 예외로 표면화.
        if (!wallet.isChargeableWithin(BigDecimal.ZERO, transaction.getAmount())) {
            throw new BusinessException(ErrorCode.WALLET_BALANCE_LIMIT_EXCEEDED);
        }

        BigDecimal balanceAfter = wallet.getBalance().add(transaction.getAmount());
        long nextSequence = wallet.getVersion() + 1;

        // 1. 원장 CREDIT append (wallet_sequence = version + 1)
        transactionEntryRepository.save(
                TransactionEntry.credit(transactionId, wallet.getId(), transaction.getAmount(), balanceAfter, nextSequence));

        // 2. 지갑 잔액/버전 갱신 (version = 최신 wallet_sequence)
        walletRepository.updateBalanceAndVersion(wallet.getId(), balanceAfter, nextSequence);

        // 3. 거래 완료 (DEPOSITING → COMPLETED, 펜싱)
        transactionRepository.updateStatusFenced(transactionId, TransactionStatus.DEPOSITING,
                TransactionStatus.COMPLETED, null, LocalDateTime.now(clock), leaseToken);
    }
}
