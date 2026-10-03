package com.example.reservation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationServiceSpecTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 3);
    @Mock CustomerService customers;
    @Mock CalendarService calendar;
    @Mock InventoryService inventory;
    @InjectMocks ReservationService service;

    private static ReservationRequest request(String customer, LocalDate date, int quantity, String price) {
        return new ReservationRequest(customer, date, quantity, price == null ? null : new BigDecimal(price));
    }

    static Stream<Arguments> invalidInputs() {
        // S01: 必須値、数量範囲、正の価格の条件をそれぞれ独立に検証する。
        return Stream.of(
            Arguments.of("SPEC-INPUT-REQUEST-NULL", null),
            Arguments.of("SPEC-INPUT-CUSTOMER-NULL", request(null, DATE, 1, "100")),
            Arguments.of("SPEC-INPUT-CUSTOMER-EMPTY", request("", DATE, 1, "100")),
            Arguments.of("SPEC-INPUT-CUSTOMER-BLANK", request(" \t\n", DATE, 1, "100")),
            Arguments.of("SPEC-INPUT-DATE-NULL", request("C1", null, 1, "100")),
            Arguments.of("SPEC-INPUT-QUANTITY-NEGATIVE", request("C1", DATE, -1, "100")),
            Arguments.of("SPEC-INPUT-QUANTITY-ZERO", request("C1", DATE, 0, "100")),
            Arguments.of("SPEC-INPUT-QUANTITY-ELEVEN", request("C1", DATE, 11, "100")),
            Arguments.of("SPEC-INPUT-PRICE-NULL", request("C1", DATE, 1, null)),
            Arguments.of("SPEC-INPUT-PRICE-ZERO", request("C1", DATE, 1, "0.00")),
            Arguments.of("SPEC-INPUT-PRICE-NEGATIVE", request("C1", DATE, 1, "-0.01"))
        );
    }

    // S01: 不正入力はIllegalArgumentException、依存先呼出しなし。例外文言は仕様未規定のため比較しない。
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidInputs")
    void rejectsInvalidInputWithoutDependencies(String id, ReservationRequest input) {
        System.out.println(id + " | spec=S01 | input=" + input + " | expected=IllegalArgumentException; no dependencies");
        assertThrows(IllegalArgumentException.class, () -> service.reserve(input), id);
        verifyNoInteractions(customers, calendar, inventory);
    }

    // S03・S04: 全会員種別×通常期/繁忙期×数量1/4/5/10を仕様の固定期待値で検証する。
    // 各行は固定ID、会員、繁忙期、数量、単価、期待割引率、期待金額の順。期待値計算は行わない。
    static Stream<Arguments> discountCases() {
        return Stream.of(
            Arguments.of("SPEC-PREMIUM-OFF-Q1", MemberType.PREMIUM, false, 1, "100", "0.10", "90"),
            Arguments.of("SPEC-PREMIUM-OFF-Q4", MemberType.PREMIUM, false, 4, "100", "0.10", "360"),
            Arguments.of("SPEC-PREMIUM-OFF-Q5", MemberType.PREMIUM, false, 5, "100", "0.10", "450"),
            Arguments.of("SPEC-PREMIUM-OFF-Q10", MemberType.PREMIUM, false, 10, "100", "0.10", "900"),
            Arguments.of("SPEC-PREMIUM-PEAK-Q1", MemberType.PREMIUM, true, 1, "100", "0.05", "95"),
            Arguments.of("SPEC-PREMIUM-PEAK-Q4", MemberType.PREMIUM, true, 4, "100", "0.05", "380"),
            Arguments.of("SPEC-PREMIUM-PEAK-Q5", MemberType.PREMIUM, true, 5, "100", "0.05", "475"),
            Arguments.of("SPEC-PREMIUM-PEAK-Q10", MemberType.PREMIUM, true, 10, "100", "0.05", "950"),
            Arguments.of("SPEC-STANDARD-OFF-Q1", MemberType.STANDARD, false, 1, "100", "0.00", "100"),
            Arguments.of("SPEC-STANDARD-OFF-Q4", MemberType.STANDARD, false, 4, "100", "0.00", "400"),
            Arguments.of("SPEC-STANDARD-OFF-Q5", MemberType.STANDARD, false, 5, "100", "0.05", "475"),
            Arguments.of("SPEC-STANDARD-OFF-Q10", MemberType.STANDARD, false, 10, "100", "0.05", "950"),
            Arguments.of("SPEC-STANDARD-PEAK-Q1", MemberType.STANDARD, true, 1, "100", "0.00", "100"),
            Arguments.of("SPEC-STANDARD-PEAK-Q4", MemberType.STANDARD, true, 4, "100", "0.00", "400"),
            Arguments.of("SPEC-STANDARD-PEAK-Q5", MemberType.STANDARD, true, 5, "100", "0.05", "475"),
            Arguments.of("SPEC-STANDARD-PEAK-Q10", MemberType.STANDARD, true, 10, "100", "0.05", "950"),
            Arguments.of("SPEC-NONE-OFF-Q1", MemberType.NONE, false, 1, "100", "0.00", "100"),
            Arguments.of("SPEC-NONE-OFF-Q4", MemberType.NONE, false, 4, "100", "0.00", "400"),
            Arguments.of("SPEC-NONE-OFF-Q5", MemberType.NONE, false, 5, "100", "0.00", "500"),
            Arguments.of("SPEC-NONE-OFF-Q10", MemberType.NONE, false, 10, "100", "0.00", "1000"),
            Arguments.of("SPEC-NONE-PEAK-Q1", MemberType.NONE, true, 1, "100", "0.00", "100"),
            Arguments.of("SPEC-NONE-PEAK-Q4", MemberType.NONE, true, 4, "100", "0.00", "400"),
            Arguments.of("SPEC-NONE-PEAK-Q5", MemberType.NONE, true, 5, "100", "0.00", "500"),
            Arguments.of("SPEC-NONE-PEAK-Q10", MemberType.NONE, true, 10, "100", "0.00", "1000"),
            Arguments.of("SPEC-STANDARD-OFF-Q6", MemberType.STANDARD, false, 6, "100", "0.05", "570"),
            Arguments.of("SPEC-STANDARD-PEAK-Q6", MemberType.STANDARD, true, 6, "100", "0.05", "570")
        );
    }

    // S01・S04: 小数価格、HALF_UPの直前/一致/直後、割引後の端数、途中丸め禁止を検証する。
    static Stream<Arguments> roundingCases() {
        return Stream.of(
            Arguments.of("SPEC-ROUND-BELOW-HALF", MemberType.NONE, false, 1, "100.49", "0.00", "100"),
            Arguments.of("SPEC-ROUND-AT-HALF", MemberType.NONE, false, 1, "100.50", "0.00", "101"),
            Arguments.of("SPEC-ROUND-ABOVE-HALF", MemberType.NONE, false, 1, "100.51", "0.00", "101"),
            Arguments.of("SPEC-ROUND-PREMIUM-HALF", MemberType.PREMIUM, false, 1, "105", "0.10", "95"),
            Arguments.of("SPEC-ROUND-STANDARD-HALF", MemberType.STANDARD, false, 6, "105", "0.05", "599"),
            Arguments.of("SPEC-ROUND-AFTER-QUANTITY", MemberType.NONE, false, 3, "0.49", "0.00", "1"),
            Arguments.of("SPEC-ROUND-AFTER-DISCOUNT", MemberType.PREMIUM, false, 3, "0.55", "0.10", "1"),
            Arguments.of("SPEC-PRICE-SMALL-POSITIVE", MemberType.NONE, false, 1, "0.01", "0.00", "0")
        );
    }

    // S02～S05: 公開入口から割引率・金額・scale=0と依存先への引数を検証する。
    @ParameterizedTest(name = "{0}")
    @MethodSource({"discountCases", "roundingCases"})
    void returnsSpecifiedPrice(String id, MemberType member, boolean peak, int quantity,
                               String unitPrice, String expectedRate, String expectedTotal) {
        assertPrice(id, DATE, member, peak, quantity, unitPrice, expectedRate, expectedTotal);
    }

    // S01: 過去・未来による制限がないことを、実行日から独立した固定日で検証する。
    @ParameterizedTest(name = "{0}")
    @MethodSource("dates")
    void acceptsPastAndFuture(String id, LocalDate date) {
        assertPrice(id, date, MemberType.NONE, false, 1, "100", "0.00", "100");
    }

    static Stream<Arguments> dates() {
        return Stream.of(
            Arguments.of("SPEC-DATE-PAST", LocalDate.of(2000, 1, 1)),
            Arguments.of("SPEC-DATE-FUTURE", LocalDate.of(2100, 1, 1))
        );
    }

    private void assertPrice(String id, LocalDate date, MemberType member, boolean peak, int quantity,
                             String price, String expectedRate, String expectedTotal) {
        when(inventory.isAvailable(date, quantity)).thenReturn(true);
        when(customers.getMemberType("C1")).thenReturn(member);
        when(calendar.isPeakSeason(date)).thenReturn(peak);
        var result = service.reserve(request("C1", date, quantity, price));
        System.out.println(id + " | spec=S01,S02,S03,S04,S05 | customer=C1 | date=" + date
            + " | member=" + member + " | peak=" + peak + " | quantity=" + quantity + " | unitPrice=" + price
            + " | expectedRate=" + expectedRate + " | actualRate=" + result.discountRate()
            + " | expectedTotal=" + expectedTotal + " | actualTotal=" + result.totalAmount()
            + " | expectedScale=0 | actualScale=" + result.totalAmount().scale());
        assertAll(id,
            () -> assertEquals(new BigDecimal(expectedRate), result.discountRate(), id + " discountRate; S03"),
            () -> assertEquals(new BigDecimal(expectedTotal), result.totalAmount(), id + " totalAmount; S04"),
            () -> assertEquals(0, result.totalAmount().scale(), id + " scale; S04"),
            () -> verify(inventory).isAvailable(date, quantity),
            () -> verify(customers).getMemberType("C1"),
            () -> verify(calendar).isPeakSeason(date));
    }

    // S02: 在庫なしで終了し、会員・カレンダー情報は取得しない。例外文言は指定しない。
    @Test
    @DisplayName("SPEC-INVENTORY-UNAVAILABLE")
    void rejectsUnavailableInventory() {
        String id = "SPEC-INVENTORY-UNAVAILABLE";
        System.out.println(id + " | spec=S02 | customer=C1 | date=" + DATE
            + " | quantity=1 | unitPrice=100 | available=false | expected=IllegalStateException; no customer/calendar");
        when(inventory.isAvailable(DATE, 1)).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> service.reserve(request("C1", DATE, 1, "100")), id);
        verify(inventory).isAvailable(DATE, 1);
        verifyNoInteractions(customers, calendar);
    }
}
