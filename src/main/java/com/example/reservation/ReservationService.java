package com.example.reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 予約の入力確認、割引率の決定、合計金額の計算をまとめるサービス。 */
public class ReservationService {
    // 会員情報、繁忙期、在庫の取得は依存先へ委ねる。
    private final CustomerService customerService;
    private final CalendarService calendarService;
    private final InventoryService inventoryService;

    public ReservationService(CustomerService customerService,
                              CalendarService calendarService,
                              InventoryService inventoryService) {
        this.customerService = customerService;
        this.calendarService = calendarService;
        this.inventoryService = inventoryService;
    }

    /** 予約条件を確認し、割引率と支払金額を返す。 */
    public ReservationResult reserve(ReservationRequest request) {
        // 外部依存先を呼ぶ前に入力を確認する。
        validate(request);
        // 在庫がない場合は、料金計算へ進まず終了する。
        if (!inventoryService.isAvailable(request.date(), request.quantity())) {
            throw new IllegalStateException("No inventory available");
        }
        // 料金の判定に使う会員情報とカレンダー情報を取得する。
        MemberType member = customerService.getMemberType(request.customerId());
        boolean peakSeason = calendarService.isPeakSeason(request.date());
        BigDecimal rate = determineDiscount(member, peakSeason, request.quantity());
        // 数量と割引率を反映し、最後に1円単位へ丸める。
        BigDecimal total = request.unitPrice()
                .multiply(BigDecimal.valueOf(request.quantity()))
                .multiply(BigDecimal.ONE.subtract(rate))
                .setScale(0, RoundingMode.HALF_UP);
        return new ReservationResult(rate, total);
    }

    /** 必須項目と入力値の範囲を確認する。 */
    private void validate(ReservationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }
        if (request.customerId() == null || request.customerId().isBlank()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (request.date() == null) {
            throw new IllegalArgumentException("Date is required");
        }
        if (request.quantity() < 1 || request.quantity() > 10) {
            throw new IllegalArgumentException("Quantity must be between 1 and 10");
        }
        if (request.unitPrice() == null || request.unitPrice().signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
    }

    /** 会員種別などの予約条件から、適用する割引率を決める。 */
    private BigDecimal determineDiscount(MemberType member, boolean peakSeason, int quantity) {
        if (quantity > 10) {
            return new BigDecimal("0.15");
        }
        if (member == MemberType.PREMIUM) {
            return new BigDecimal("0.10");
        }
        if (member == MemberType.STANDARD && quantity > 5) {
            return new BigDecimal("0.05");
        }
        return new BigDecimal("0.00");
    }
}
