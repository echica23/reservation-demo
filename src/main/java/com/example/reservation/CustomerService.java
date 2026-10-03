package com.example.reservation;

/** 顧客情報の取得先。UTではMockに置き換える。 */
public interface CustomerService {
    /** 顧客IDに対応する会員種別を返す。 */
    MemberType getMemberType(String customerId);
}
