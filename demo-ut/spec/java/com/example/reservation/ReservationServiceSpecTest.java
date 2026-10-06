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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationServiceSpecTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 6);
    @Mock CustomerService customers;
    @Mock CalendarService calendar;
    @Mock InventoryService inventory;

    private ReservationService service() {
        return new ReservationService(customers, calendar, inventory);
    }

    private static ReservationRequest request(String customer, LocalDate date, int quantity, String price) {
        return new ReservationRequest(customer, date, quantity,
                price == null ? null : new BigDecimal(price));
    }

    // S01: 各必須項目、数量の範囲外、非正価格を独立して検証。メッセージ文言は仕様外。
    static Stream<Arguments> invalidInputs() {
        return Stream.of(
            Arguments.of("SPEC-INPUT-REQUEST-NULL", null),
            Arguments.of("SPEC-INPUT-CUSTOMER-NULL", request(null, DATE, 1, "100")),
            Arguments.of("SPEC-INPUT-CUSTOMER-EMPTY", request("", DATE, 1, "100")),
            Arguments.of("SPEC-INPUT-CUSTOMER-BLANK", request(" \t", DATE, 1, "100")),
            Arguments.of("SPEC-INPUT-DATE-NULL", request("C1", null, 1, "100")),
            Arguments.of("SPEC-INPUT-QUANTITY-NEGATIVE", request("C1", DATE, -1, "100")),
            Arguments.of("SPEC-INPUT-QUANTITY-ZERO", request("C1", DATE, 0, "100")),
            Arguments.of("SPEC-INPUT-QUANTITY-ELEVEN", request("C1", DATE, 11, "100")),
            Arguments.of("SPEC-INPUT-PRICE-NULL", request("C1", DATE, 1, null)),
            Arguments.of("SPEC-INPUT-PRICE-ZERO", request("C1", DATE, 1, "0")),
            Arguments.of("SPEC-INPUT-PRICE-NEGATIVE", request("C1", DATE, 1, "-0.01"))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidInputs")
    void rejectsInvalidInput(String id, ReservationRequest input) {
        System.out.println(id + " | spec=S01 | input=" + input + " | expected=IllegalArgumentException");
        var error = assertThrows(IllegalArgumentException.class, () -> service().reserve(input), id);
        System.out.println(id + " | actual=" + error.getClass().getSimpleName());
        verifyNoInteractions(inventory, customers, calendar);
    }

    // S01/S03/S04: 数量1・4・5・6・10と全会員・繁忙期を組み合わせ、固定の仕様期待値で検証。
    // 各行: ID, 会員, 繁忙期, 数量, 単価, 期待割引率, 期待金額。
    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "SPEC-NONE-OFF-Q1,NONE,false,1,100,0.00,100",
        "SPEC-NONE-OFF-Q4,NONE,false,4,100,0.00,400",
        "SPEC-NONE-OFF-Q5,NONE,false,5,100,0.00,500",
        "SPEC-NONE-OFF-Q6,NONE,false,6,100,0.00,600",
        "SPEC-NONE-OFF-Q10,NONE,false,10,100,0.00,1000",
        "SPEC-NONE-PEAK-Q1,NONE,true,1,100,0.00,100",
        "SPEC-NONE-PEAK-Q4,NONE,true,4,100,0.00,400",
        "SPEC-NONE-PEAK-Q5,NONE,true,5,100,0.00,500",
        "SPEC-NONE-PEAK-Q6,NONE,true,6,100,0.00,600",
        "SPEC-NONE-PEAK-Q10,NONE,true,10,100,0.00,1000",
        "SPEC-STANDARD-OFF-Q1,STANDARD,false,1,100,0.00,100",
        "SPEC-STANDARD-OFF-Q4,STANDARD,false,4,100,0.00,400",
        "SPEC-STANDARD-OFF-Q5,STANDARD,false,5,100,0.05,475",
        "SPEC-STANDARD-OFF-Q6,STANDARD,false,6,100,0.05,570",
        "SPEC-STANDARD-OFF-Q10,STANDARD,false,10,100,0.05,950",
        "SPEC-STANDARD-PEAK-Q1,STANDARD,true,1,100,0.00,100",
        "SPEC-STANDARD-PEAK-Q4,STANDARD,true,4,100,0.00,400",
        "SPEC-STANDARD-PEAK-Q5,STANDARD,true,5,100,0.05,475",
        "SPEC-STANDARD-PEAK-Q6,STANDARD,true,6,100,0.05,570",
        "SPEC-STANDARD-PEAK-Q10,STANDARD,true,10,100,0.05,950",
        "SPEC-PREMIUM-OFF-Q1,PREMIUM,false,1,100,0.10,90",
        "SPEC-PREMIUM-OFF-Q4,PREMIUM,false,4,100,0.10,360",
        "SPEC-PREMIUM-OFF-Q5,PREMIUM,false,5,100,0.10,450",
        "SPEC-PREMIUM-OFF-Q6,PREMIUM,false,6,100,0.10,540",
        "SPEC-PREMIUM-OFF-Q10,PREMIUM,false,10,100,0.10,900",
        "SPEC-PREMIUM-PEAK-Q1,PREMIUM,true,1,100,0.05,95",
        "SPEC-PREMIUM-PEAK-Q4,PREMIUM,true,4,100,0.05,380",
        "SPEC-PREMIUM-PEAK-Q5,PREMIUM,true,5,100,0.05,475",
        "SPEC-PREMIUM-PEAK-Q6,PREMIUM,true,6,100,0.05,570",
        "SPEC-PREMIUM-PEAK-Q10,PREMIUM,true,10,100,0.05,950"
    })
    void returnsSpecifiedPrice(String id, MemberType member, boolean peak, int quantity,
                               String price, String rate, String total) {
        assertPrice(id, DATE, member, peak, quantity, price, rate, total);
    }

    // S04: HALF_UPの直前・一致・直後、割引後の丸め、途中丸め禁止、正の小数価格。
    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "SPEC-ROUND-BELOW,NONE,false,1,0.49,0.00,0",
        "SPEC-ROUND-HALF,NONE,false,1,0.50,0.00,1",
        "SPEC-ROUND-ABOVE,NONE,false,1,0.51,0.00,1",
        "SPEC-ROUND-PREMIUM,PREMIUM,false,1,1.67,0.10,2",
        "SPEC-ROUND-STANDARD,STANDARD,false,6,0.10,0.05,1",
        "SPEC-ROUND-NO-INTERMEDIATE,NONE,false,3,0.49,0.00,1"
    })
    void roundsOnlyFinalTotal(String id, MemberType member, boolean peak, int quantity,
                              String price, String rate, String total) {
        assertPrice(id, DATE, member, peak, quantity, price, rate, total);
    }

    // S01: 現在時刻に依存せず、過去・未来の日付を受け付ける。
    @ParameterizedTest(name = "{0}")
    @CsvSource({"SPEC-DATE-PAST,2000-01-01", "SPEC-DATE-FUTURE,2100-01-01"})
    void acceptsPastAndFuture(String id, LocalDate date) {
        assertPrice(id, date, MemberType.NONE, false, 1, "100", "0.00", "100");
    }

    // S02/S05: 公開入口から在庫不足例外と後続依存の未呼び出しを確認する。
    @Test
    @DisplayName("SPEC-INVENTORY-UNAVAILABLE")
    void rejectsUnavailableInventory() {
        String id = "SPEC-INVENTORY-UNAVAILABLE";
        when(inventory.isAvailable(DATE, 1)).thenReturn(false);
        System.out.println(id + " | spec=S02 | quantity=1 | date=" + DATE
                + " | available=false | expected=IllegalStateException");
        var error = assertThrows(IllegalStateException.class,
                () -> service().reserve(request("C1", DATE, 1, "100")), id);
        System.out.println(id + " | actual=" + error.getClass().getSimpleName());
        verify(inventory).isAvailable(DATE, 1);
        verifyNoInteractions(customers, calendar);
        verifyNoMoreInteractions(inventory);
    }

    // S02/S03/S04/S05: Mock準備と検証のみを共有。期待値の計算・仕様分岐は行わない。
    private void assertPrice(String id, LocalDate date, MemberType member, boolean peak,
                             int quantity, String price, String rate, String total) {
        when(inventory.isAvailable(date, quantity)).thenReturn(true);
        when(customers.getMemberType("C1")).thenReturn(member);
        when(calendar.isPeakSeason(date)).thenReturn(peak);
        var result = service().reserve(request("C1", date, quantity, price));
        System.out.println(id + " | spec=S01,S02,S03,S04,S05 | customer=C1 | date=" + date
                + " | member=" + member + " | peak=" + peak + " | quantity=" + quantity
                + " | unitPrice=" + price + " | expectedRate=" + rate
                + " | actualRate=" + result.discountRate() + " | expectedTotal=" + total
                + " | actualTotal=" + result.totalAmount() + " | expectedScale=0"
                + " | actualScale=" + result.totalAmount().scale());
        verify(inventory).isAvailable(date, quantity);
        verify(customers).getMemberType("C1");
        verify(calendar).isPeakSeason(date);
        verifyNoMoreInteractions(inventory, customers, calendar);
        assertAll(id,
            () -> assertEquals(new BigDecimal(rate), result.discountRate(), id + " S03 rate"),
            () -> assertEquals(new BigDecimal(total), result.totalAmount(), id + " S04 total"),
            () -> assertEquals(0, result.totalAmount().scale(), id + " S04 scale")
        );
    }
}
