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
class ReservationServiceTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 3);
    @Mock CustomerService customers;
    @Mock CalendarService calendar;
    @Mock InventoryService inventory;
    @InjectMocks ReservationService service;

    static ReservationRequest request(String customer, LocalDate date, int quantity, String price) {
        return new ReservationRequest(customer, date, quantity, price == null ? null : new BigDecimal(price));
    }

    static Stream<Arguments> invalidInputs() {
        return Stream.of(
            Arguments.of("INPUT-REQUEST-NULL", null, "Request is required"),
            Arguments.of("INPUT-CUSTOMER-NULL", request(null, DATE, 1, "100"), "Customer ID is required"),
            Arguments.of("INPUT-CUSTOMER-EMPTY", request("", DATE, 1, "100"), "Customer ID is required"),
            Arguments.of("INPUT-CUSTOMER-BLANK", request(" \t\n", DATE, 1, "100"), "Customer ID is required"),
            Arguments.of("INPUT-DATE-NULL", request("C1", null, 1, "100"), "Date is required"),
            Arguments.of("INPUT-QUANTITY-ZERO", request("C1", DATE, 0, "100"), "Quantity must be between 1 and 10"),
            Arguments.of("INPUT-QUANTITY-NEGATIVE", request("C1", DATE, -1, "100"), "Quantity must be between 1 and 10"),
            Arguments.of("INPUT-QUANTITY-ELEVEN", request("C1", DATE, 11, "100"), "Quantity must be between 1 and 10"),
            Arguments.of("INPUT-PRICE-NULL", request("C1", DATE, 1, null), "Unit price must be positive"),
            Arguments.of("INPUT-PRICE-ZERO", request("C1", DATE, 1, "0"), "Unit price must be positive"),
            Arguments.of("INPUT-PRICE-NEGATIVE", request("C1", DATE, 1, "-0.01"), "Unit price must be positive")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidInputs")
    void rejectsInvalidInput(String id, ReservationRequest input, String message) {
        System.out.println(id + " | input=" + input + " | expectedException=" + message);
        var error = assertThrows(IllegalArgumentException.class, () -> service.reserve(input), id);
        assertEquals(message, error.getMessage(), id);
        verifyNoInteractions(customers, calendar, inventory);
    }

    static Stream<Arguments> prices() {
        // 固定期待値は対象実装の観測可能な挙動に基づく。業務仕様への適合は判断しない。
        return Stream.of(
            Arguments.of("DISCOUNT-PREMIUM-OFF-Q1", MemberType.PREMIUM, false, 1, "100", "0.10", "90"),
            Arguments.of("DISCOUNT-PREMIUM-PEAK-Q1", MemberType.PREMIUM, true, 1, "100", "0.10", "90"),
            Arguments.of("DISCOUNT-PREMIUM-OFF-Q10", MemberType.PREMIUM, false, 10, "100", "0.10", "900"),
            Arguments.of("DISCOUNT-PREMIUM-PEAK-Q10", MemberType.PREMIUM, true, 10, "100", "0.10", "900"),
            Arguments.of("DISCOUNT-STANDARD-OFF-Q1", MemberType.STANDARD, false, 1, "100", "0.00", "100"),
            Arguments.of("DISCOUNT-STANDARD-OFF-Q5", MemberType.STANDARD, false, 5, "100", "0.00", "500"),
            Arguments.of("DISCOUNT-STANDARD-PEAK-Q5", MemberType.STANDARD, true, 5, "100", "0.00", "500"),
            Arguments.of("DISCOUNT-STANDARD-OFF-Q6", MemberType.STANDARD, false, 6, "100", "0.05", "570"),
            Arguments.of("DISCOUNT-STANDARD-PEAK-Q6", MemberType.STANDARD, true, 6, "100", "0.05", "570"),
            Arguments.of("DISCOUNT-STANDARD-OFF-Q10", MemberType.STANDARD, false, 10, "100", "0.05", "950"),
            Arguments.of("DISCOUNT-NONE-OFF-Q1", MemberType.NONE, false, 1, "100", "0.00", "100"),
            Arguments.of("DISCOUNT-NONE-PEAK-Q10", MemberType.NONE, true, 10, "100", "0.00", "1000"),
            Arguments.of("DISCOUNT-MEMBER-NULL", null, false, 6, "100", "0.00", "600"),
            Arguments.of("ROUND-NONE-BELOW-HALF", MemberType.NONE, false, 1, "100.49", "0.00", "100"),
            Arguments.of("ROUND-NONE-HALF", MemberType.NONE, false, 1, "100.50", "0.00", "101"),
            Arguments.of("ROUND-NONE-ABOVE-HALF", MemberType.NONE, false, 1, "100.51", "0.00", "101"),
            Arguments.of("ROUND-PREMIUM-HALF", MemberType.PREMIUM, false, 1, "105", "0.10", "95"),
            Arguments.of("ROUND-STANDARD-HALF", MemberType.STANDARD, false, 6, "105", "0.05", "599"),
            Arguments.of("ROUND-AFTER-QUANTITY", MemberType.NONE, false, 3, "0.49", "0.00", "1"),
            Arguments.of("PRICE-POSITIVE-SMALL", MemberType.NONE, false, 1, "0.01", "0.00", "0")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("prices")
    void computesObservedPrice(String id, MemberType member, boolean peak, int quantity,
                               String price, String expectedRate, String expectedTotal) {
        System.out.println(id + " | member=" + member + " | peak=" + peak + " | quantity=" + quantity
            + " | unitPrice=" + price + " | expectedRate=" + expectedRate + " | expectedTotal=" + expectedTotal);
        when(inventory.isAvailable(DATE, quantity)).thenReturn(true);
        when(customers.getMemberType("C1")).thenReturn(member);
        when(calendar.isPeakSeason(DATE)).thenReturn(peak);
        var result = service.reserve(request("C1", DATE, quantity, price));
        assertAll(id,
            () -> assertEquals(new BigDecimal(expectedRate), result.discountRate(), id),
            () -> assertEquals(new BigDecimal(expectedTotal), result.totalAmount(), id));
        var order = inOrder(inventory, customers, calendar);
        order.verify(inventory).isAvailable(DATE, quantity);
        order.verify(customers).getMemberType("C1");
        order.verify(calendar).isPeakSeason(DATE);
        verifyNoMoreInteractions(inventory, customers, calendar);
    }

    @Test
    @DisplayName("INVENTORY-UNAVAILABLE")
    void rejectsUnavailableInventory() {
        String id = "INVENTORY-UNAVAILABLE";
        System.out.println(id + " | customer=C1 | date=" + DATE + " | quantity=1 | unitPrice=100 | available=false");
        when(inventory.isAvailable(DATE, 1)).thenReturn(false);
        var error = assertThrows(IllegalStateException.class,
            () -> service.reserve(request("C1", DATE, 1, "100")), id);
        assertEquals("No inventory available", error.getMessage(), id);
        verify(inventory).isAvailable(DATE, 1);
        verifyNoMoreInteractions(inventory);
        verifyNoInteractions(customers, calendar);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dependencyFailures")
    void propagatesDependencyFailure(String id, String failingDependency) {
        System.out.println(id + " | customer=C1 | date=" + DATE + " | quantity=1 | unitPrice=100 | failing=" + failingDependency);
        var error = new IllegalStateException(id);
        if (failingDependency.equals("inventory")) {
            when(inventory.isAvailable(DATE, 1)).thenThrow(error);
        } else {
            when(inventory.isAvailable(DATE, 1)).thenReturn(true);
            if (failingDependency.equals("customer")) {
                when(customers.getMemberType("C1")).thenThrow(error);
            } else {
                when(customers.getMemberType("C1")).thenReturn(MemberType.NONE);
                when(calendar.isPeakSeason(DATE)).thenThrow(error);
            }
        }
        assertSame(error, assertThrows(IllegalStateException.class,
            () -> service.reserve(request("C1", DATE, 1, "100")), id), id);
        verify(inventory).isAvailable(DATE, 1);
        if (!failingDependency.equals("inventory")) verify(customers).getMemberType("C1");
        if (failingDependency.equals("calendar")) verify(calendar).isPeakSeason(DATE);
        verifyNoMoreInteractions(inventory, customers, calendar);
    }

    static Stream<Arguments> dependencyFailures() {
        return Stream.of(
            Arguments.of("DEPENDENCY-INVENTORY-THROWS", "inventory"),
            Arguments.of("DEPENDENCY-CUSTOMER-THROWS", "customer"),
            Arguments.of("DEPENDENCY-CALENDAR-THROWS", "calendar")
        );
    }
}
