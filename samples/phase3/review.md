# Phase3：ReservationService レビュー

配布用コピー（元の出力: C:\demo\reservation-demo\target\review\20261006-113220-53c9ff）。実行ID: 20261006-113220-53c9ff。Phase2のUTを変更せず再実行し、50件中43件成功・7件FAILを確認した。
根拠: [仕様](../../docs/reservation-spec.md)、[実装](../../src/main/java/com/example/reservation/ReservationService.java)、[UT](../../demo-ut/spec/java/com/example/reservation/ReservationServiceSpecTest.java)、[今回の実測](case-results.json)。全件の実行・計測条件は[report.md](report.md)を参照。

## R1：PREMIUM繁忙期の割引率が仕様と不一致

分類: 業務仕様不一致。確度: 高（仕様・実装・固定期待値・実測が一致して原因を示す）。

箇所: ReservationService.java 65〜66行目。PREMIUMで常に0.10を返し、引数peakSeasonを参照していない。reserveでは32行目で繁忙期を取得し33行目で渡しているため、Mock値が未設定であることが原因ではない。

仕様S03は繁忙期5%、通常期10%。対応IDはSPEC-PREMIUM-PEAK-Q1、Q4、Q5、Q6、Q10（それぞれ末尾を置換した5件）。単価100の実測で過剰な割引により仕様より支払額が小さくなる。通常期の対応ケースは成功している。

修正案: PREMIUM分岐内でpeakSeasonを判定し、trueなら0.05、falseなら0.10を返す。金額計算式やテスト期待値を変更する必要はない。

## R2：STANDARD数量5の割引が適用されない

分類: 業務仕様不一致。確度: 高。

箇所: ReservationService.java 68行目。quantity > 5では数量5が対象外になり、71行目の0.00へ進む。仕様S03は数量5以上で5%。数量4と6の境界周辺ケースは成功しており、数量5の不一致を区別できる。

対応ID: SPEC-STANDARD-OFF-Q5、SPEC-STANDARD-PEAK-Q5。期待率0.05・金額475に対し、実際は0.00・500。繁忙期に関係なく発生する。

修正案: 比較をquantity >= 5へ変更する。仕様の境界を変えたり、期待値を0%に合わせたりしない。

## R3：公開入口から到達しない15%割引処理

分類: 到達不能コード／保守上の問題。業務仕様FAIL7件の原因ではない。確度: 高（現在の公開入口・呼出し経路に限定）。

箇所: ReservationService.java 62〜64行目。reserveは25行目でvalidateを呼び、52〜53行目で数量10超を例外にする。その後でのみdetermineDiscountへ進む。ReservationRequestはrecordでquantityが変更されないため、検証通過後に数量10超へ変わる経路もない。

対応ID: SPEC-INPUT-QUANTITY-ELEVENはIllegalArgumentExceptionと外部依存未呼出しを確認してPASS。正常系の数量10ケースも存在する。JaCoCoでは62行目true側が未実行、63行目が未カバー。この説明は未カバーだけからの推測ではなく、ソースの条件と呼出し順による。

修正案: 現行仕様を維持する実務上の整理ならquantity > 10の分岐を削除する。大口割引を有効化するための入力制約緩和は業務仕様変更であり、今回の修正案に含めない。デモの到達不能コードの教材として保持する選択とは分けて扱う。

## 不一致ケースの実測

全件、顧客C1・日付2026-10-06・単価100・在庫あり。根拠はS03/S04。
| ケースID | 会員 | 繁忙期 | 数量 | 期待率 | 実際率 | 期待金額 | 実際金額 |
| --- | --- | --- | ---: | ---: | ---: | ---: | ---: |
| SPEC-STANDARD-OFF-Q5 | STANDARD | false | 5 | 0.05 | 0.00 | 475 | 500 |
| SPEC-STANDARD-PEAK-Q5 | STANDARD | true | 5 | 0.05 | 0.00 | 475 | 500 |
| SPEC-PREMIUM-PEAK-Q1 | PREMIUM | true | 1 | 0.05 | 0.10 | 95 | 90 |
| SPEC-PREMIUM-PEAK-Q4 | PREMIUM | true | 4 | 0.05 | 0.10 | 380 | 360 |
| SPEC-PREMIUM-PEAK-Q5 | PREMIUM | true | 5 | 0.05 | 0.10 | 475 | 450 |
| SPEC-PREMIUM-PEAK-Q6 | PREMIUM | true | 6 | 0.05 | 0.10 | 570 | 540 |
| SPEC-PREMIUM-PEAK-Q10 | PREMIUM | true | 10 | 0.05 | 0.10 | 950 | 900 |

## 修正案のコード例（未適用・未検証）

R1/R2を修正し、R3を整理する場合の例。これは提案であり本番ソースへ適用していない。

```java
private BigDecimal determineDiscount(MemberType member, boolean peakSeason, int quantity) {
    if (member == MemberType.PREMIUM) {
        return new BigDecimal(peakSeason ? "0.05" : "0.10");
    }
    if (member == MemberType.STANDARD && quantity >= 5) {
        return new BigDecimal("0.05");
    }
    return new BigDecimal("0.00");
}
```

別途適用する場合は、今回のPhase2 UT全件で繁忙期、数量4/5/6、通常期、丸め、数量11の拒否が維持されることを再検証する。修正後のPASSやカバレッジ値はまだ測定していない。

## UT・仕様の確認と限界

7件の期待値はS03/S04の固定値で、実装と同じ計算式をUT内で再現していない。依存は各ケースで設定され、ERROR/SKIPは0件。コンパイル・環境不備、UT不備、仕様不足がこのFAILの原因である証拠はない。
指定仕様の対象外である依存障害・並行処理・永続化は検証しない。既存UTは今回のレビューで追加・修正していない。

## 変更確認

[再利用確認](reuse-check.json)・[変更確認](change-check.json)で、Phase2のソース・仕様・UTとの一致と今回の未変更を確認。本番への修正、ビルド設定変更、README更新、Git公開は行っていない。

## トークン使用量（概算）

このレビューを含む同一コマンドの概算・計測区間・制約は[実行レポート末尾](report.md#トークン使用量概算)と[token-usage.json](token-usage.json)を参照。report.mdと重複して加算しない。

