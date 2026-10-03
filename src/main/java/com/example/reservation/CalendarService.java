package com.example.reservation;

import java.time.LocalDate;

/** 予約日のカレンダー情報の取得先。UTではMockに置き換える。 */
public interface CalendarService {
    /** 指定日が繁忙期に該当するかを返す。 */
    boolean isPeakSeason(LocalDate date);
}
