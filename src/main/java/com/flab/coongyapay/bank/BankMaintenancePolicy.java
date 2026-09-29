package com.flab.coongyapay.bank;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class BankMaintenancePolicy {

    private static final LocalTime MAINTENANCE_START_TIME = LocalTime.MIDNIGHT;
    private static final LocalTime MAINTENANCE_END_TIME = LocalTime.of(0, 30);

    private final Clock clock;

    public boolean isMaintenanceTime() {
        LocalTime now = LocalTime.now(clock);
        return !now.isBefore(MAINTENANCE_START_TIME) && !now.isAfter(MAINTENANCE_END_TIME);
    }
}
