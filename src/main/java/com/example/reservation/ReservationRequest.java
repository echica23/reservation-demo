package com.example.reservation;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 予約の入力値。顧客ID、予約日、予約数、1件あたりの価格を保持する。 */
public record ReservationRequest(String customerId, LocalDate date,
                                 int quantity, BigDecimal unitPrice) {}
