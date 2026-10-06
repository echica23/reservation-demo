# Phase2：仕様基準UT 実行レポート

実行ID: 20261006-112631-e91918 / 実行日: 2026-10-06（Asia/Tokyo）

配布用コピー。元の実行ID: 20261006-112631-e91918 / 元の出力: C:\demo\reservation-demo\target\spec\20261006-112631-e91918

## 実行概要

コマンド: `ut -P "C:\demo\reservation-demo" -spec com.example.reservation.ReservationService -D docs/reservation-spec.md`

対象: C:\demo\reservation-demo の com.example.reservation.ReservationService（公開入口reserve）。
参照: [業務仕様 S01〜S05](../../docs/reservation-spec.md)、対象ソースと依存インターフェース、DTO、pom.xml、実行指示書。

Phase1の既存UTを保持することをユーザーが承認。Phase2は仕様から新規生成し、Phase1 UTの内容・期待値・過去のレビューは参照していない。会話に既存デモの背景があるため独立したAI実験とはしない。

## 生成UTと仕様の対応

[ReservationServiceSpecTest.java](../../demo-ut/spec/java/com/example/reservation/ReservationServiceSpecTest.java) を新規作成。JUnit5／Mockitoで3依存をMock化し、対象自体は実体を使用。privateメソッドは直接呼ばない。

| ケース群 | 件数 | 仕様・狙い |
| --- | ---: | --- |
| SPEC-INPUT-* | 11 | S01：必須入力、null/空文字/空白、数量範囲外、非正価格。例外型と依存未呼び出し |
| SPEC-NONE/ STANDARD/ PREMIUM-* | 30 | S01/S03/S04：全会員×繁忙期有無×数量1/4/5/6/10。固定割引率・固定金額 |
| SPEC-ROUND-* | 6 | S04：HALF_UP直前/一致/直後、割引後の丸め、途中丸め禁止、scale=0 |
| SPEC-DATE-* | 2 | S01：過去・未来とも許可 |
| SPEC-INVENTORY-UNAVAILABLE | 1 | S02：在庫不足例外と後続依存未呼び出し |

正常系では依存に渡した日付・数量・顧客ID、呼出し回数も検証。各ケースのコメント・固定期待値はUTに記載。例外メッセージは仕様にないためassertしない。

## 実行結果

実行 50件 / 成功 43件 / FAIL 7件 / ERROR 0件 / SKIP 0件。
Maven test終了コード1（アサーションFAIL）、jacoco:report終了コード0。コンパイル・起動・計測のエラーはない。

実行したコマンド（記録用。同じIDで再実行せず、再実行時は未使用のIDへ変更する）：

```powershell
& "C:\Users\saram\.m2\wrapper\dists\apache-maven-3.9.9-bin\33b4b2b4\apache-maven-3.9.9\bin\mvn.cmd" "-Pphase2,coverage" "-Ddemo.runId=20261006-112631-e91918" test
& "C:\Users\saram\.m2\wrapper\dists\apache-maven-3.9.9-bin\33b4b2b4\apache-maven-3.9.9\bin\mvn.cmd" "-Pphase2,coverage" "-Ddemo.runId=20261006-112631-e91918" jacoco:report
```

[テストログ](test.log) / [計測ログ](coverage.log) / [Surefire XML](surefire-reports/TEST-com.example.reservation.ReservationServiceSpecTest.xml) / [ケース別結果](case-results.json) / [機械集計](summary.json)

## カバレッジ

分母はReservationServiceのみ。依存クラス・DTO・テストコードを含めない。

| 指標 | covered | missed | 割合 |
| --- | ---: | ---: | ---: |
| BRANCH | 25 | 1 | 96.15% |
| LINE | 33 | 1 | 97.06% |

[対象ソース](../../src/main/java/com/example/reservation/ReservationService.java) の62行目に未実行のtrue分岐、63行目に未実行のreturnがある。公開入口は先に数量1〜10を検証するため、数量10超は割引処理まで到達しない。数量11の入力ケースはS01の例外として成功した。
Line 100%は未達。未カバーを消すためのprivate直接呼出し・仕様外期待値・本番変更・除外設定は行わない。この未カバーと以下の仕様FAILは別の事象。

[対象クラスHTML](site/jacoco/com.example.reservation/ReservationService.html) / [JaCoCo XML](site/jacoco/jacoco.xml)

## 仕様不一致

全件とも顧客C1、日付2026-10-06、単価100。在庫あり、会員・繁忙期は表の条件。割引率はS03、金額はS04を根拠とする。

| ケースID | 会員 | 繁忙期 | 数量 | 期待割引率 | 実際割引率 | 期待金額 | 実際金額 |
| --- | --- | --- | ---: | ---: | ---: | ---: | ---: |
| SPEC-STANDARD-OFF-Q5 | STANDARD | false | 5 | 0.05 | 0.00 | 475 | 500 |
| SPEC-STANDARD-PEAK-Q5 | STANDARD | true | 5 | 0.05 | 0.00 | 475 | 500 |
| SPEC-PREMIUM-PEAK-Q1 | PREMIUM | true | 1 | 0.05 | 0.10 | 95 | 90 |
| SPEC-PREMIUM-PEAK-Q4 | PREMIUM | true | 4 | 0.05 | 0.10 | 380 | 360 |
| SPEC-PREMIUM-PEAK-Q5 | PREMIUM | true | 5 | 0.05 | 0.10 | 475 | 450 |
| SPEC-PREMIUM-PEAK-Q6 | PREMIUM | true | 6 | 0.05 | 0.10 | 570 | 540 |
| SPEC-PREMIUM-PEAK-Q10 | PREMIUM | true | 10 | 0.05 | 0.10 | 950 | 900 |

固定期待値を実装に合わせて変更していない。詳細な実装原因分析・修正案はPhase3の対象であり、本レポートでは実施しない。

## 全ケースの入力・期待値・実測

固定IDはJUnit表示名・アサーション・system-outに記載。50件のIDが一意で、Surefire XMLと対応することを確認した。

| ケースID | 結果 | 条件・期待値・実測 |
| --- | --- | --- |
| SPEC-INVENTORY-UNAVAILABLE | PASS | SPEC-INVENTORY-UNAVAILABLE  /  spec=S02  /  quantity=1  /  date=2026-10-06  /  available=false  /  expected=IllegalStateException<br>SPEC-INVENTORY-UNAVAILABLE  /  actual=IllegalStateException |
| SPEC-NONE-OFF-Q1 | PASS | SPEC-NONE-OFF-Q1  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=NONE  /  peak=false  /  quantity=1  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=100  /  actualTotal=100  /  expectedScale=0  /  actualScale=0 |
| SPEC-NONE-OFF-Q4 | PASS | SPEC-NONE-OFF-Q4  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=NONE  /  peak=false  /  quantity=4  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=400  /  actualTotal=400  /  expectedScale=0  /  actualScale=0 |
| SPEC-NONE-OFF-Q5 | PASS | SPEC-NONE-OFF-Q5  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=NONE  /  peak=false  /  quantity=5  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=500  /  actualTotal=500  /  expectedScale=0  /  actualScale=0 |
| SPEC-NONE-OFF-Q6 | PASS | SPEC-NONE-OFF-Q6  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=NONE  /  peak=false  /  quantity=6  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=600  /  actualTotal=600  /  expectedScale=0  /  actualScale=0 |
| SPEC-NONE-OFF-Q10 | PASS | SPEC-NONE-OFF-Q10  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=NONE  /  peak=false  /  quantity=10  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=1000  /  actualTotal=1000  /  expectedScale=0  /  actualScale=0 |
| SPEC-NONE-PEAK-Q1 | PASS | SPEC-NONE-PEAK-Q1  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=NONE  /  peak=true  /  quantity=1  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=100  /  actualTotal=100  /  expectedScale=0  /  actualScale=0 |
| SPEC-NONE-PEAK-Q4 | PASS | SPEC-NONE-PEAK-Q4  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=NONE  /  peak=true  /  quantity=4  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=400  /  actualTotal=400  /  expectedScale=0  /  actualScale=0 |
| SPEC-NONE-PEAK-Q5 | PASS | SPEC-NONE-PEAK-Q5  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=NONE  /  peak=true  /  quantity=5  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=500  /  actualTotal=500  /  expectedScale=0  /  actualScale=0 |
| SPEC-NONE-PEAK-Q6 | PASS | SPEC-NONE-PEAK-Q6  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=NONE  /  peak=true  /  quantity=6  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=600  /  actualTotal=600  /  expectedScale=0  /  actualScale=0 |
| SPEC-NONE-PEAK-Q10 | PASS | SPEC-NONE-PEAK-Q10  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=NONE  /  peak=true  /  quantity=10  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=1000  /  actualTotal=1000  /  expectedScale=0  /  actualScale=0 |
| SPEC-STANDARD-OFF-Q1 | PASS | SPEC-STANDARD-OFF-Q1  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=STANDARD  /  peak=false  /  quantity=1  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=100  /  actualTotal=100  /  expectedScale=0  /  actualScale=0 |
| SPEC-STANDARD-OFF-Q4 | PASS | SPEC-STANDARD-OFF-Q4  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=STANDARD  /  peak=false  /  quantity=4  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=400  /  actualTotal=400  /  expectedScale=0  /  actualScale=0 |
| SPEC-STANDARD-OFF-Q5 | FAIL | SPEC-STANDARD-OFF-Q5  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=STANDARD  /  peak=false  /  quantity=5  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.00  /  expectedTotal=475  /  actualTotal=500  /  expectedScale=0  /  actualScale=0 |
| SPEC-STANDARD-OFF-Q6 | PASS | SPEC-STANDARD-OFF-Q6  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=STANDARD  /  peak=false  /  quantity=6  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.05  /  expectedTotal=570  /  actualTotal=570  /  expectedScale=0  /  actualScale=0 |
| SPEC-STANDARD-OFF-Q10 | PASS | SPEC-STANDARD-OFF-Q10  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=STANDARD  /  peak=false  /  quantity=10  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.05  /  expectedTotal=950  /  actualTotal=950  /  expectedScale=0  /  actualScale=0 |
| SPEC-STANDARD-PEAK-Q1 | PASS | SPEC-STANDARD-PEAK-Q1  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=STANDARD  /  peak=true  /  quantity=1  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=100  /  actualTotal=100  /  expectedScale=0  /  actualScale=0 |
| SPEC-STANDARD-PEAK-Q4 | PASS | SPEC-STANDARD-PEAK-Q4  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=STANDARD  /  peak=true  /  quantity=4  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=400  /  actualTotal=400  /  expectedScale=0  /  actualScale=0 |
| SPEC-STANDARD-PEAK-Q5 | FAIL | SPEC-STANDARD-PEAK-Q5  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=STANDARD  /  peak=true  /  quantity=5  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.00  /  expectedTotal=475  /  actualTotal=500  /  expectedScale=0  /  actualScale=0 |
| SPEC-STANDARD-PEAK-Q6 | PASS | SPEC-STANDARD-PEAK-Q6  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=STANDARD  /  peak=true  /  quantity=6  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.05  /  expectedTotal=570  /  actualTotal=570  /  expectedScale=0  /  actualScale=0 |
| SPEC-STANDARD-PEAK-Q10 | PASS | SPEC-STANDARD-PEAK-Q10  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=STANDARD  /  peak=true  /  quantity=10  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.05  /  expectedTotal=950  /  actualTotal=950  /  expectedScale=0  /  actualScale=0 |
| SPEC-PREMIUM-OFF-Q1 | PASS | SPEC-PREMIUM-OFF-Q1  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=PREMIUM  /  peak=false  /  quantity=1  /  unitPrice=100  /  expectedRate=0.10  /  actualRate=0.10  /  expectedTotal=90  /  actualTotal=90  /  expectedScale=0  /  actualScale=0 |
| SPEC-PREMIUM-OFF-Q4 | PASS | SPEC-PREMIUM-OFF-Q4  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=PREMIUM  /  peak=false  /  quantity=4  /  unitPrice=100  /  expectedRate=0.10  /  actualRate=0.10  /  expectedTotal=360  /  actualTotal=360  /  expectedScale=0  /  actualScale=0 |
| SPEC-PREMIUM-OFF-Q5 | PASS | SPEC-PREMIUM-OFF-Q5  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=PREMIUM  /  peak=false  /  quantity=5  /  unitPrice=100  /  expectedRate=0.10  /  actualRate=0.10  /  expectedTotal=450  /  actualTotal=450  /  expectedScale=0  /  actualScale=0 |
| SPEC-PREMIUM-OFF-Q6 | PASS | SPEC-PREMIUM-OFF-Q6  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=PREMIUM  /  peak=false  /  quantity=6  /  unitPrice=100  /  expectedRate=0.10  /  actualRate=0.10  /  expectedTotal=540  /  actualTotal=540  /  expectedScale=0  /  actualScale=0 |
| SPEC-PREMIUM-OFF-Q10 | PASS | SPEC-PREMIUM-OFF-Q10  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=PREMIUM  /  peak=false  /  quantity=10  /  unitPrice=100  /  expectedRate=0.10  /  actualRate=0.10  /  expectedTotal=900  /  actualTotal=900  /  expectedScale=0  /  actualScale=0 |
| SPEC-PREMIUM-PEAK-Q1 | FAIL | SPEC-PREMIUM-PEAK-Q1  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=PREMIUM  /  peak=true  /  quantity=1  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.10  /  expectedTotal=95  /  actualTotal=90  /  expectedScale=0  /  actualScale=0 |
| SPEC-PREMIUM-PEAK-Q4 | FAIL | SPEC-PREMIUM-PEAK-Q4  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=PREMIUM  /  peak=true  /  quantity=4  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.10  /  expectedTotal=380  /  actualTotal=360  /  expectedScale=0  /  actualScale=0 |
| SPEC-PREMIUM-PEAK-Q5 | FAIL | SPEC-PREMIUM-PEAK-Q5  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=PREMIUM  /  peak=true  /  quantity=5  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.10  /  expectedTotal=475  /  actualTotal=450  /  expectedScale=0  /  actualScale=0 |
| SPEC-PREMIUM-PEAK-Q6 | FAIL | SPEC-PREMIUM-PEAK-Q6  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=PREMIUM  /  peak=true  /  quantity=6  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.10  /  expectedTotal=570  /  actualTotal=540  /  expectedScale=0  /  actualScale=0 |
| SPEC-PREMIUM-PEAK-Q10 | FAIL | SPEC-PREMIUM-PEAK-Q10  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=PREMIUM  /  peak=true  /  quantity=10  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.10  /  expectedTotal=950  /  actualTotal=900  /  expectedScale=0  /  actualScale=0 |
| SPEC-INPUT-REQUEST-NULL | PASS | SPEC-INPUT-REQUEST-NULL  /  spec=S01  /  input=null  /  expected=IllegalArgumentException<br>SPEC-INPUT-REQUEST-NULL  /  actual=IllegalArgumentException |
| SPEC-INPUT-CUSTOMER-NULL | PASS | SPEC-INPUT-CUSTOMER-NULL  /  spec=S01  /  input=ReservationRequest[customerId=null, date=2026-10-06, quantity=1, unitPrice=100]  /  expected=IllegalArgumentException<br>SPEC-INPUT-CUSTOMER-NULL  /  actual=IllegalArgumentException |
| SPEC-INPUT-CUSTOMER-EMPTY | PASS | SPEC-INPUT-CUSTOMER-EMPTY  /  spec=S01  /  input=ReservationRequest[customerId=, date=2026-10-06, quantity=1, unitPrice=100]  /  expected=IllegalArgumentException<br>SPEC-INPUT-CUSTOMER-EMPTY  /  actual=IllegalArgumentException |
| SPEC-INPUT-CUSTOMER-BLANK | PASS | SPEC-INPUT-CUSTOMER-BLANK  /  spec=S01  /  input=ReservationRequest[customerId= 	, date=2026-10-06, quantity=1, unitPrice=100]  /  expected=IllegalArgumentException<br>SPEC-INPUT-CUSTOMER-BLANK  /  actual=IllegalArgumentException |
| SPEC-INPUT-DATE-NULL | PASS | SPEC-INPUT-DATE-NULL  /  spec=S01  /  input=ReservationRequest[customerId=C1, date=null, quantity=1, unitPrice=100]  /  expected=IllegalArgumentException<br>SPEC-INPUT-DATE-NULL  /  actual=IllegalArgumentException |
| SPEC-INPUT-QUANTITY-NEGATIVE | PASS | SPEC-INPUT-QUANTITY-NEGATIVE  /  spec=S01  /  input=ReservationRequest[customerId=C1, date=2026-10-06, quantity=-1, unitPrice=100]  /  expected=IllegalArgumentException<br>SPEC-INPUT-QUANTITY-NEGATIVE  /  actual=IllegalArgumentException |
| SPEC-INPUT-QUANTITY-ZERO | PASS | SPEC-INPUT-QUANTITY-ZERO  /  spec=S01  /  input=ReservationRequest[customerId=C1, date=2026-10-06, quantity=0, unitPrice=100]  /  expected=IllegalArgumentException<br>SPEC-INPUT-QUANTITY-ZERO  /  actual=IllegalArgumentException |
| SPEC-INPUT-QUANTITY-ELEVEN | PASS | SPEC-INPUT-QUANTITY-ELEVEN  /  spec=S01  /  input=ReservationRequest[customerId=C1, date=2026-10-06, quantity=11, unitPrice=100]  /  expected=IllegalArgumentException<br>SPEC-INPUT-QUANTITY-ELEVEN  /  actual=IllegalArgumentException |
| SPEC-INPUT-PRICE-NULL | PASS | SPEC-INPUT-PRICE-NULL  /  spec=S01  /  input=ReservationRequest[customerId=C1, date=2026-10-06, quantity=1, unitPrice=null]  /  expected=IllegalArgumentException<br>SPEC-INPUT-PRICE-NULL  /  actual=IllegalArgumentException |
| SPEC-INPUT-PRICE-ZERO | PASS | SPEC-INPUT-PRICE-ZERO  /  spec=S01  /  input=ReservationRequest[customerId=C1, date=2026-10-06, quantity=1, unitPrice=0]  /  expected=IllegalArgumentException<br>SPEC-INPUT-PRICE-ZERO  /  actual=IllegalArgumentException |
| SPEC-INPUT-PRICE-NEGATIVE | PASS | SPEC-INPUT-PRICE-NEGATIVE  /  spec=S01  /  input=ReservationRequest[customerId=C1, date=2026-10-06, quantity=1, unitPrice=-0.01]  /  expected=IllegalArgumentException<br>SPEC-INPUT-PRICE-NEGATIVE  /  actual=IllegalArgumentException |
| SPEC-ROUND-BELOW | PASS | SPEC-ROUND-BELOW  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=NONE  /  peak=false  /  quantity=1  /  unitPrice=0.49  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=0  /  actualTotal=0  /  expectedScale=0  /  actualScale=0 |
| SPEC-ROUND-HALF | PASS | SPEC-ROUND-HALF  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=NONE  /  peak=false  /  quantity=1  /  unitPrice=0.50  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=1  /  actualTotal=1  /  expectedScale=0  /  actualScale=0 |
| SPEC-ROUND-ABOVE | PASS | SPEC-ROUND-ABOVE  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=NONE  /  peak=false  /  quantity=1  /  unitPrice=0.51  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=1  /  actualTotal=1  /  expectedScale=0  /  actualScale=0 |
| SPEC-ROUND-PREMIUM | PASS | SPEC-ROUND-PREMIUM  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=PREMIUM  /  peak=false  /  quantity=1  /  unitPrice=1.67  /  expectedRate=0.10  /  actualRate=0.10  /  expectedTotal=2  /  actualTotal=2  /  expectedScale=0  /  actualScale=0 |
| SPEC-ROUND-STANDARD | PASS | SPEC-ROUND-STANDARD  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=STANDARD  /  peak=false  /  quantity=6  /  unitPrice=0.10  /  expectedRate=0.05  /  actualRate=0.05  /  expectedTotal=1  /  actualTotal=1  /  expectedScale=0  /  actualScale=0 |
| SPEC-ROUND-NO-INTERMEDIATE | PASS | SPEC-ROUND-NO-INTERMEDIATE  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-06  /  member=NONE  /  peak=false  /  quantity=3  /  unitPrice=0.49  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=1  /  actualTotal=1  /  expectedScale=0  /  actualScale=0 |
| SPEC-DATE-PAST | PASS | SPEC-DATE-PAST  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2000-01-01  /  member=NONE  /  peak=false  /  quantity=1  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=100  /  actualTotal=100  /  expectedScale=0  /  actualScale=0 |
| SPEC-DATE-FUTURE | PASS | SPEC-DATE-FUTURE  /  spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2100-01-01  /  member=NONE  /  peak=false  /  quantity=1  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=100  /  actualTotal=100  /  expectedScale=0  /  actualScale=0 |

## 制約と変更確認

依存障害、null会員、並行予約、永続化はS02により対象外。今回選定したケースで期待値未確定はない。有限のケースであり全入力の正しさを保証しない。UTは初回実行でコンパイル・実行でき、再試行なし。
本番ソース・pom.xml・仕様・README・実行指示書・既存Phase1 UTのSHA-256一致を確認。変更はPhase2の生成UTと実行成果物のみ。
[開始時ハッシュ](before-hashes.json) / [終了時ハッシュ](after-hashes.json) / [変更確認](change-check.json) / [生成UTハッシュ](ut-hashes.json)

## トークン使用量（概算）

入力 987902、うちキャッシュ 980096、出力 4815、合計 992717。参考reasoning_output_tokens: 385。内数を再加算しない。
計測区間: 10/06/2026 02:26:40 〜 10/06/2026 02:29:28。コマンド境界前の累計ではなく、開始時に取得できた最新累計を基準にした。
概算・最終計測時点まで。初期読取りと計測開始前の処理、最終計測後のレポート作成・コピー・検証・最終回答を含まない。確認待ちを挟んだ同一コマンドの区間。費用換算なし。
[計測根拠](token-usage.json)

