package com.example.reservation;

import java.time.LocalDate;

/** 予約可能な在庫の確認先。UTではMockに置き換える。 */
public interface InventoryService {
    /** 指定日と予約数に対して在庫を確保できるかを返す。 */
    boolean isAvailable(LocalDate date, int quantity);
}
