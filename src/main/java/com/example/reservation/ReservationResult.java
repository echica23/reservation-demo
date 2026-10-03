package com.example.reservation;

import java.math.BigDecimal;

/** 計算結果。割引率は小数で表し、合計金額は1円単位で保持する。 */
public record ReservationResult(BigDecimal discountRate, BigDecimal totalAmount) {}
