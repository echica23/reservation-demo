# Phase3 レビュー：ReservationService

実行日：2026-10-03（Asia/Tokyo）。実行ID：`20261003-181648`。対象は `com.example.reservation.ReservationService`。
元出力：`C:\demo\reservation-demo\target\review\20261003-181648`。これは上記出力の配布用コピー。

Phase2のUT・固定ケースID・期待値・狙いコメントを変更せず再利用した。今回も48件中42件成功、6件失敗、エラー・スキップ0。全48件の入力・期待値・実測値・成否がPhase2と一致した。[実行レポート](report.md) / [ケース対応](case-results.json) / [Surefire XML](surefire-reports/TEST-com.example.reservation.ReservationServiceSpecTest.xml)。

## R1：PREMIUM繁忙期に通常期の割引率を返す

- 区分：仕様と実装の不一致。確度：高（仕様、コード、UT、再現実行の照合で確認済み）。
- 箇所：[ReservationService.java](../../src/main/java/com/example/reservation/ReservationService.java) の62～63行。31～32行で取得・引き渡したpeakSeasonを、determineDiscount内で参照していない。
- 根拠：[仕様S03](../../docs/reservation-spec.md) はPREMIUM通常期10%、繁忙期5%、数量による変化なしと規定する。
- 対応ケース：`SPEC-PREMIUM-PEAK-Q1`、`SPEC-PREMIUM-PEAK-Q4`、`SPEC-PREMIUM-PEAK-Q5`、`SPEC-PREMIUM-PEAK-Q10`。
- 確認した事実：Mockのカレンダーはtrueを返し、会員はPREMIUM。実装はPREMIUMなら無条件に0.10を返す。期待0.05に対して実際0.10で、支払金額も仕様より小さくなる。単価100・数量1なら95円のところ90円。
- 影響範囲：有効なPREMIUM繁忙期予約。数量1/4/5/10で再現した。コード上は数量に依存しない分岐なので、他の有効数量でも割引率不一致が生じると判断するが、今回全数量を実測したわけではない。
- 修正案：PREMIUMの分岐でpeakSeasonを判定し、trueなら0.05、falseなら0.10を返す。

## R2：STANDARDの割引境界が予約数6からになっている

- 区分：仕様と実装の不一致。確度：高（仕様、コード、UT、再現実行の照合で確認済み）。
- 箇所：[ReservationService.java](../../src/main/java/com/example/reservation/ReservationService.java) の65行。現在の条件は `quantity > 5`。
- 根拠：[仕様S03](../../docs/reservation-spec.md) はSTANDARDの予約数5以上を5%、1～4を0%、繁忙期による変化なしと規定する。
- 対応ケース：`SPEC-STANDARD-OFF-Q5`、`SPEC-STANDARD-PEAK-Q5`。
- 確認した事実：数量5では条件がfalseとなり、68行の0.00へ進む。両季節とも期待0.05に対し実際0.00、単価100・数量5の金額は期待475円に対し実際500円。
- 影響範囲：STANDARDの予約数5。数量4・6・10の対照ケースは成功し、境界の不一致を裏付ける。
- 修正案：条件を `quantity >= 5` にする。STANDARDでは繁忙期の条件を追加しない。

## 失敗ケースの実測対応

すべて顧客C1、予約日2026-10-03、在庫あり。金額の単位は円。

| 固定ケースID | 会員／繁忙期／数量／単価 | 割引率 期待→実際 | 金額 期待→実際 | 仕様／指摘 |
| --- | --- | --- | --- | --- |
| SPEC-PREMIUM-PEAK-Q1 | PREMIUM / true / 1 / 100 | 0.05 → 0.10 | 95 → 90 | S03 繁忙期5%、S04 / R1 |
| SPEC-PREMIUM-PEAK-Q4 | PREMIUM / true / 4 / 100 | 0.05 → 0.10 | 380 → 360 | S03 繁忙期5%、S04 / R1 |
| SPEC-PREMIUM-PEAK-Q5 | PREMIUM / true / 5 / 100 | 0.05 → 0.10 | 475 → 450 | S03 繁忙期5%、S04 / R1 |
| SPEC-PREMIUM-PEAK-Q10 | PREMIUM / true / 10 / 100 | 0.05 → 0.10 | 950 → 900 | S03 繁忙期5%、S04 / R1 |
| SPEC-STANDARD-OFF-Q5 | STANDARD / false / 5 / 100 | 0.05 → 0.00 | 475 → 500 | S03 5以上5%、S04 / R2 |
| SPEC-STANDARD-PEAK-Q5 | STANDARD / true / 5 / 100 | 0.05 → 0.00 | 475 → 500 | S03 5以上5%、S04 / R2 |

6件の失敗テストにそれぞれ割引率・金額の2つのアサーション不一致がある。失敗テスト数は12件ではなく6件。

## 原因の切り分け

コンパイル・環境エラーはなく、依存先のMockは仕様で許される会員・繁忙期の値を返している。固定期待値をS03・S04と照合しており、この6件をUT不備や仕様不足に分類する根拠はない。失敗したという事実だけでなく、該当条件分岐と期待値の根拠から実装との不一致と判断した。

金額計算の34～37行は価格×数量×(1−割引率)と最終HALF_UPを実行している。今回の6件の金額不一致は誤った割引率が計算に入ることで説明でき、金額計算式の独立した不備を示すものではない。丸め8ケースは成功しているが、全入力の正しさを証明したとは扱わない。

Line 32/32（100%）、Branch 24/24（100%）でも上記の不一致が残る。カバレッジは実行された実装経路の指標であり、仕様に必要な分岐の存在や条件の正しさを保証しない。

## 実装修正案（未適用・未検証）

対象メソッドの修正例を示す。production code・UTへは適用していない。

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

修正後の確認案は、既存48ケースを期待値・IDを保持したまま未使用実行IDで再実行すること。6件のFAIL解消と既存42件の成功維持、coverageの再計測を確認する。これは今後の検証計画であり、修正後の成功やcoverageは未実測。修正で分岐が増えるため、現時点の24分岐という分母も修正後へ流用しない。

## 制約・変更確認・使用量

レビューのためのUT再生成・追加・書換え、仕様変更、本番コード修正は行っていない。Phase2保存時との一致と今回の開始前後の一致をSHA-256で確認した。[変更確認](change-check.json) / [Phase2との同一性](phase2-reuse-check.json)。依存先障害・並行予約・永続化など仕様対象外の問題は未評価。

トークン使用量は[実行レポート末尾](report.md#token-usage)に今回の概算値・基準値・計測区間・制約を記載した。同じPhase3処理なのでreport.mdとreview.mdの2件分として重複加算しない。

