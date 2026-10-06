package com.example.reservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 6);
    private CustomerService customers;
    private CalendarService calendar;
    private InventoryService inventory;
    private ReservationService service;

    @BeforeEach
    void setUp() {
        customers = mock(CustomerService.class);
        calendar = mock(CalendarService.class);
        inventory = mock(InventoryService.class);
        service = new ReservationService(customers, calendar, inventory);
    }

    static Stream<Arguments> invalidInputs() {
        return Stream.of(
            Arguments.of("INPUT-REQUEST-NULL", null, "Request is required"),
            Arguments.of("INPUT-CUSTOMER-NULL", new ReservationRequest(null, DATE, 1, new BigDecimal("100")), "Customer ID is required"),
            Arguments.of("INPUT-CUSTOMER-EMPTY", new ReservationRequest("", DATE, 1, new BigDecimal("100")), "Customer ID is required"),
            Arguments.of("INPUT-CUSTOMER-BLANK", new ReservationRequest(" \t", DATE, 1, new BigDecimal("100")), "Customer ID is required"),
            Arguments.of("INPUT-DATE-NULL", new ReservationRequest("C1", null, 1, new BigDecimal("100")), "Date is required"),
            Arguments.of("INPUT-QUANTITY-ZERO", new ReservationRequest("C1", DATE, 0, new BigDecimal("100")), "Quantity must be between 1 and 10"),
            Arguments.of("INPUT-QUANTITY-NEGATIVE", new ReservationRequest("C1", DATE, -1, new BigDecimal("100")), "Quantity must be between 1 and 10"),
            Arguments.of("INPUT-QUANTITY-ELEVEN", new ReservationRequest("C1", DATE, 11, new BigDecimal("100")), "Quantity must be between 1 and 10"),
            Arguments.of("INPUT-PRICE-NULL", new ReservationRequest("C1", DATE, 1, null), "Unit price must be positive"),
            Arguments.of("INPUT-PRICE-ZERO", new ReservationRequest("C1", DATE, 1, new BigDecimal("0")), "Unit price must be positive"),
            Arguments.of("INPUT-PRICE-NEGATIVE", new ReservationRequest("C1", DATE, 1, new BigDecimal("-0.01")), "Unit price must be positive")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidInputs")
    void rejectsInvalidInput(String id, ReservationRequest request, String message) {
        System.out.println("CASE-ID=" + id);
        var error = assertThrows(IllegalArgumentException.class, () -> service.reserve(request), id);
        assertEquals(message, error.getMessage(), id);
        verifyNoInteractions(customers, calendar, inventory);
    }

    // Fixed expected values describe the current implementation, not business specification.
    static Stream<Arguments> prices() {
        return Stream.of(
            Arguments.of("DISCOUNT-NONE-OFF-Q1", MemberType.NONE, false, 1, "100", "0.00", "100"),
            Arguments.of("DISCOUNT-NONE-PEAK-Q10", MemberType.NONE, true, 10, "100", "0.00", "1000"),
            Arguments.of("DISCOUNT-STANDARD-OFF-Q1", MemberType.STANDARD, false, 1, "100", "0.00", "100"),
            Arguments.of("DISCOUNT-STANDARD-OFF-Q5", MemberType.STANDARD, false, 5, "100", "0.00", "500"),
            Arguments.of("DISCOUNT-STANDARD-PEAK-Q5", MemberType.STANDARD, true, 5, "100", "0.00", "500"),
            Arguments.of("DISCOUNT-STANDARD-OFF-Q6", MemberType.STANDARD, false, 6, "100", "0.05", "570"),
            Arguments.of("DISCOUNT-STANDARD-PEAK-Q6", MemberType.STANDARD, true, 6, "100", "0.05", "570"),
            Arguments.of("DISCOUNT-STANDARD-OFF-Q10", MemberType.STANDARD, false, 10, "100", "0.05", "950"),
            Arguments.of("DISCOUNT-PREMIUM-OFF-Q1", MemberType.PREMIUM, false, 1, "100", "0.10", "90"),
            Arguments.of("DISCOUNT-PREMIUM-PEAK-Q1", MemberType.PREMIUM, true, 1, "100", "0.10", "90"),
            Arguments.of("DISCOUNT-PREMIUM-OFF-Q5", MemberType.PREMIUM, false, 5, "100", "0.10", "450"),
            Arguments.of("DISCOUNT-PREMIUM-PEAK-Q10", MemberType.PREMIUM, true, 10, "100", "0.10", "900"),
            Arguments.of("DISCOUNT-MEMBER-NULL", null, false, 1, "100", "0.00", "100"),
            Arguments.of("ROUND-NONE-BELOW-HALF", MemberType.NONE, false, 1, "100.49", "0.00", "100"),
            Arguments.of("ROUND-NONE-AT-HALF", MemberType.NONE, false, 1, "100.50", "0.00", "101"),
            Arguments.of("ROUND-PREMIUM-BELOW-HALF", MemberType.PREMIUM, false, 1, "100.55", "0.10", "90"),
            Arguments.of("ROUND-PREMIUM-ABOVE-HALF", MemberType.PREMIUM, false, 1, "100.56", "0.10", "91"),
            Arguments.of("ROUND-AFTER-QUANTITY", MemberType.NONE, false, 2, "100.25", "0.00", "201"),
            Arguments.of("ROUND-STANDARD-AT-HALF", MemberType.STANDARD, false, 10, "101", "0.05", "960"),
            Arguments.of("ROUND-POSITIVE-TO-ZERO", MemberType.NONE, false, 1, "0.01", "0.00", "0")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("prices")
    void calculatesPrice(String id, MemberType member, boolean peak, int quantity,
                         String unitPrice, String rate, String total) {
        System.out.println("CASE-ID=" + id);
        when(inventory.isAvailable(DATE, quantity)).thenReturn(true);
        when(customers.getMemberType("C1")).thenReturn(member);
        when(calendar.isPeakSeason(DATE)).thenReturn(peak);
        var result = service.reserve(new ReservationRequest("C1", DATE, quantity, new BigDecimal(unitPrice)));
        assertAll(id,
            () -> assertEquals(new BigDecimal(rate), result.discountRate(), id + " rate"),
            () -> assertEquals(new BigDecimal(total), result.totalAmount(), id + " total (scale 0)"));
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
        System.out.println("CASE-ID=" + id);
        when(inventory.isAvailable(DATE, 1)).thenReturn(false);
        var error = assertThrows(IllegalStateException.class,
            () -> service.reserve(new ReservationRequest("C1", DATE, 1, new BigDecimal("100"))), id);
        assertEquals("No inventory available", error.getMessage(), id);
        verify(inventory).isAvailable(DATE, 1);
        verifyNoInteractions(customers, calendar);
        verifyNoMoreInteractions(inventory);
    }
}

