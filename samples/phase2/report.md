# Phase2 仕様基準UTレポート：ReservationService

## 1. 実行概要

- 入力：`ut -P "C:\demo\reservation-demo" -spec com.example.reservation.ReservationService -D docs/reservation-spec.md`
- Phase2、実行日2026-10-03（Asia/Tokyo）、実行ID：`20261003-180427`。
- プロジェクト：`C:\demo\reservation-demo`。実際の対象は `com.example.reservation.ReservationService` の1クラス。
- 指定仕様：[reservation-spec.md](../../docs/reservation-spec.md) のS01～S05を正として期待値を決定した。
- 実装・関連型・依存先インターフェースは呼出し方法と依存関係を把握するために参照。pom.xmlと今回の実行結果・JaCoCoも参照した。
- Phase1の期待値、READMEサンプル結果、過去の成果物を期待値の根拠として使用していない。Phase1保存物は保全確認のためハッシュだけを計測した。
- 元の出力：`C:\demo\reservation-demo\target\spec\20261003-180427`。これは上記出力の配布用コピー。

## 2. 生成・利用したUT

[ReservationServiceSpecTest.java](../../demo-ut/spec/java/com/example/reservation/ReservationServiceSpecTest.java) を新規生成。今回のPhase1 UTは既知の生成物として保持し、Phase2には混ぜない。Phase2開始前にspec UTは存在しなかった。

JUnit5＋Mockitoで3依存先をMock化。公開入口reserveからのみ検証。各UT・ケース表に狙いとS01～S05の対応コメントを記載した。金額・割引率はケースごとの固定値で、期待値を条件分岐や本番と同じ計算式で作っていない。

| 仕様 | テスト範囲 | 件数 |
| --- | --- | ---: |
| S01 | 必須項目、空文字・空白、数量0/-1/11、価格null/0/負数と依存先未呼出し | 11 |
| S02 | 在庫なしの例外、会員・カレンダー未呼出し | 1 |
| S03・S04 | 全会員×通常期/繁忙期×数量1/4/5/10、STANDARD数量6 | 26 |
| S01・S04 | 小数価格、0.5直前/一致/直後、割引後端数、途中丸め禁止、正の微小価格 | 8 |
| S01 | 過去2000-01-01・未来2100-01-01の日付を受理 | 2 |

正常系では依存先引数、割引率、金額、金額scale=0も検証。顧客はC1、日付は特記がなければ2026-10-03。例外の文言、依存先障害、null会員は仕様に根拠がない／対象外なので期待値を追加しない。

48件の一意な固定ケースIDをSurefire各testcaseのsystem-outへ記録し、入力・期待値・実測値と対応付けた。表示名とアサーションメッセージにもIDを付与。JUnit実行順番号だけで識別しない。[全ケース対応データ](case-results.json)。

| 固定ケースID | 仕様との対応・入力・期待値・実測値 | 結果 |
| --- | --- | --- |
| SPEC-INVENTORY-UNAVAILABLE | spec=S02  /  customer=C1  /  date=2026-10-03  /  quantity=1  /  unitPrice=100  /  available=false  /  expected=IllegalStateException; no customer/calendar | PASS |
| SPEC-PREMIUM-OFF-Q1 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=PREMIUM  /  peak=false  /  quantity=1  /  unitPrice=100  /  expectedRate=0.10  /  actualRate=0.10  /  expectedTotal=90  /  actualTotal=90  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-PREMIUM-OFF-Q4 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=PREMIUM  /  peak=false  /  quantity=4  /  unitPrice=100  /  expectedRate=0.10  /  actualRate=0.10  /  expectedTotal=360  /  actualTotal=360  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-PREMIUM-OFF-Q5 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=PREMIUM  /  peak=false  /  quantity=5  /  unitPrice=100  /  expectedRate=0.10  /  actualRate=0.10  /  expectedTotal=450  /  actualTotal=450  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-PREMIUM-OFF-Q10 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=PREMIUM  /  peak=false  /  quantity=10  /  unitPrice=100  /  expectedRate=0.10  /  actualRate=0.10  /  expectedTotal=900  /  actualTotal=900  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-PREMIUM-PEAK-Q1 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=PREMIUM  /  peak=true  /  quantity=1  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.10  /  expectedTotal=95  /  actualTotal=90  /  expectedScale=0  /  actualScale=0 | FAIL |
| SPEC-PREMIUM-PEAK-Q4 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=PREMIUM  /  peak=true  /  quantity=4  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.10  /  expectedTotal=380  /  actualTotal=360  /  expectedScale=0  /  actualScale=0 | FAIL |
| SPEC-PREMIUM-PEAK-Q5 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=PREMIUM  /  peak=true  /  quantity=5  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.10  /  expectedTotal=475  /  actualTotal=450  /  expectedScale=0  /  actualScale=0 | FAIL |
| SPEC-PREMIUM-PEAK-Q10 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=PREMIUM  /  peak=true  /  quantity=10  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.10  /  expectedTotal=950  /  actualTotal=900  /  expectedScale=0  /  actualScale=0 | FAIL |
| SPEC-STANDARD-OFF-Q1 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=STANDARD  /  peak=false  /  quantity=1  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=100  /  actualTotal=100  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-STANDARD-OFF-Q4 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=STANDARD  /  peak=false  /  quantity=4  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=400  /  actualTotal=400  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-STANDARD-OFF-Q5 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=STANDARD  /  peak=false  /  quantity=5  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.00  /  expectedTotal=475  /  actualTotal=500  /  expectedScale=0  /  actualScale=0 | FAIL |
| SPEC-STANDARD-OFF-Q10 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=STANDARD  /  peak=false  /  quantity=10  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.05  /  expectedTotal=950  /  actualTotal=950  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-STANDARD-PEAK-Q1 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=STANDARD  /  peak=true  /  quantity=1  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=100  /  actualTotal=100  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-STANDARD-PEAK-Q4 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=STANDARD  /  peak=true  /  quantity=4  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=400  /  actualTotal=400  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-STANDARD-PEAK-Q5 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=STANDARD  /  peak=true  /  quantity=5  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.00  /  expectedTotal=475  /  actualTotal=500  /  expectedScale=0  /  actualScale=0 | FAIL |
| SPEC-STANDARD-PEAK-Q10 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=STANDARD  /  peak=true  /  quantity=10  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.05  /  expectedTotal=950  /  actualTotal=950  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-NONE-OFF-Q1 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=NONE  /  peak=false  /  quantity=1  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=100  /  actualTotal=100  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-NONE-OFF-Q4 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=NONE  /  peak=false  /  quantity=4  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=400  /  actualTotal=400  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-NONE-OFF-Q5 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=NONE  /  peak=false  /  quantity=5  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=500  /  actualTotal=500  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-NONE-OFF-Q10 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=NONE  /  peak=false  /  quantity=10  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=1000  /  actualTotal=1000  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-NONE-PEAK-Q1 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=NONE  /  peak=true  /  quantity=1  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=100  /  actualTotal=100  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-NONE-PEAK-Q4 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=NONE  /  peak=true  /  quantity=4  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=400  /  actualTotal=400  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-NONE-PEAK-Q5 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=NONE  /  peak=true  /  quantity=5  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=500  /  actualTotal=500  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-NONE-PEAK-Q10 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=NONE  /  peak=true  /  quantity=10  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=1000  /  actualTotal=1000  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-STANDARD-OFF-Q6 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=STANDARD  /  peak=false  /  quantity=6  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.05  /  expectedTotal=570  /  actualTotal=570  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-STANDARD-PEAK-Q6 | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=STANDARD  /  peak=true  /  quantity=6  /  unitPrice=100  /  expectedRate=0.05  /  actualRate=0.05  /  expectedTotal=570  /  actualTotal=570  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-ROUND-BELOW-HALF | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=NONE  /  peak=false  /  quantity=1  /  unitPrice=100.49  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=100  /  actualTotal=100  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-ROUND-AT-HALF | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=NONE  /  peak=false  /  quantity=1  /  unitPrice=100.50  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=101  /  actualTotal=101  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-ROUND-ABOVE-HALF | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=NONE  /  peak=false  /  quantity=1  /  unitPrice=100.51  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=101  /  actualTotal=101  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-ROUND-PREMIUM-HALF | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=PREMIUM  /  peak=false  /  quantity=1  /  unitPrice=105  /  expectedRate=0.10  /  actualRate=0.10  /  expectedTotal=95  /  actualTotal=95  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-ROUND-STANDARD-HALF | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=STANDARD  /  peak=false  /  quantity=6  /  unitPrice=105  /  expectedRate=0.05  /  actualRate=0.05  /  expectedTotal=599  /  actualTotal=599  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-ROUND-AFTER-QUANTITY | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=NONE  /  peak=false  /  quantity=3  /  unitPrice=0.49  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=1  /  actualTotal=1  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-ROUND-AFTER-DISCOUNT | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=PREMIUM  /  peak=false  /  quantity=3  /  unitPrice=0.55  /  expectedRate=0.10  /  actualRate=0.10  /  expectedTotal=1  /  actualTotal=1  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-PRICE-SMALL-POSITIVE | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2026-10-03  /  member=NONE  /  peak=false  /  quantity=1  /  unitPrice=0.01  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=0  /  actualTotal=0  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-INPUT-REQUEST-NULL | spec=S01  /  input=null  /  expected=IllegalArgumentException; no dependencies | PASS |
| SPEC-INPUT-CUSTOMER-NULL | spec=S01  /  input=ReservationRequest[customerId=null, date=2026-10-03, quantity=1, unitPrice=100]  /  expected=IllegalArgumentException; no dependencies | PASS |
| SPEC-INPUT-CUSTOMER-EMPTY | spec=S01  /  input=ReservationRequest[customerId=, date=2026-10-03, quantity=1, unitPrice=100]  /  expected=IllegalArgumentException; no dependencies | PASS |
| SPEC-INPUT-CUSTOMER-BLANK | spec=S01  /  input=ReservationRequest[customerId= \t\n, date=2026-10-03, quantity=1, unitPrice=100]  /  expected=IllegalArgumentException; no dependencies | PASS |
| SPEC-INPUT-DATE-NULL | spec=S01  /  input=ReservationRequest[customerId=C1, date=null, quantity=1, unitPrice=100]  /  expected=IllegalArgumentException; no dependencies | PASS |
| SPEC-INPUT-QUANTITY-NEGATIVE | spec=S01  /  input=ReservationRequest[customerId=C1, date=2026-10-03, quantity=-1, unitPrice=100]  /  expected=IllegalArgumentException; no dependencies | PASS |
| SPEC-INPUT-QUANTITY-ZERO | spec=S01  /  input=ReservationRequest[customerId=C1, date=2026-10-03, quantity=0, unitPrice=100]  /  expected=IllegalArgumentException; no dependencies | PASS |
| SPEC-INPUT-QUANTITY-ELEVEN | spec=S01  /  input=ReservationRequest[customerId=C1, date=2026-10-03, quantity=11, unitPrice=100]  /  expected=IllegalArgumentException; no dependencies | PASS |
| SPEC-INPUT-PRICE-NULL | spec=S01  /  input=ReservationRequest[customerId=C1, date=2026-10-03, quantity=1, unitPrice=null]  /  expected=IllegalArgumentException; no dependencies | PASS |
| SPEC-INPUT-PRICE-ZERO | spec=S01  /  input=ReservationRequest[customerId=C1, date=2026-10-03, quantity=1, unitPrice=0.00]  /  expected=IllegalArgumentException; no dependencies | PASS |
| SPEC-INPUT-PRICE-NEGATIVE | spec=S01  /  input=ReservationRequest[customerId=C1, date=2026-10-03, quantity=1, unitPrice=-0.01]  /  expected=IllegalArgumentException; no dependencies | PASS |
| SPEC-DATE-PAST | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2000-01-01  /  member=NONE  /  peak=false  /  quantity=1  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=100  /  actualTotal=100  /  expectedScale=0  /  actualScale=0 | PASS |
| SPEC-DATE-FUTURE | spec=S01,S02,S03,S04,S05  /  customer=C1  /  date=2100-01-01  /  member=NONE  /  peak=false  /  quantity=1  /  unitPrice=100  /  expectedRate=0.00  /  actualRate=0.00  /  expectedTotal=100  /  actualTotal=100  /  expectedScale=0  /  actualScale=0 | PASS |

## 3. 実行結果

**48件中42件成功、6件失敗、エラー0、スキップ0。** Maven test終了コード1（テストFAIL）。コンパイル・環境エラーは発生していない。JaCoCoレポート生成は別途実施し、終了コード0。

Phase2専用プロファイルで実行し、コンパイルされたUTはReservationServiceSpecTestだけであることを確認した。仕様に沿った期待値を保持し、FAILの無効化・スキップ・削除はしていない。

以下は実行時の記録。再実行時はdemo.runIdを新しい未使用IDに変更し、testとjacoco:reportで同じIDを用いる。testがFAILでもjacoco:reportを実行する。PowerShell構文解析済み。[コマンドと終了コードの確認](command-check.json)。

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.11'
Set-Location 'C:\demo\reservation-demo'
& "C:\Users\saram\.m2\wrapper\dists\apache-maven-3.9.9-bin\33b4b2b4\apache-maven-3.9.9\bin\mvn.cmd" "-Pphase2,coverage" "-Ddemo.runId=20261003-180427" "-Dmaven.repo.local=C:/Users/saram/.m2/repository" test
& "C:\Users\saram\.m2\wrapper\dists\apache-maven-3.9.9-bin\33b4b2b4\apache-maven-3.9.9\bin\mvn.cmd" "-Pphase2,coverage" "-Ddemo.runId=20261003-180427" "-Dmaven.repo.local=C:/Users/saram/.m2/repository" jacoco:report
```

[実行ログ](test.log) / [Surefire XML](surefire-reports/TEST-com.example.reservation.ReservationServiceSpecTest.xml) / [Surefire概要](surefire-reports/com.example.reservation.ReservationServiceSpecTest.txt) / [JaCoCo生成ログ](coverage.log)

## 4. Coverage

対象クラスReservationServiceのXMLカウンタのみを集計。Mock化した依存先、Request/Result、enum、UTは分母に含めない。

| 指標 | covered | missed | 割合 |
| --- | ---: | ---: | ---: |
| Line Coverage | 32 | 0 | 100% |
| Branch Coverage | 24 | 0 | 100% |

Line 100%の目標達成、未カバー行・分岐なし。今回の失敗したテストを含む実行から得られた実測値であり、業務仕様への適合とは別の指標である。

[対象クラスHTML](site/jacoco/com.example.reservation/ReservationService.html) / [HTMLトップ](site/jacoco/index.html) / [XML](site/jacoco/jacoco.xml) / [CSV](site/jacoco/jacoco.csv) / [jacoco.exec](jacoco.exec)

## 5. 仕様不一致

以下の6件は、指定仕様S03の割引率およびS04による期待金額と実際の結果が一致しないことを、仕様・UTの固定値・Surefire結果で照合した。各失敗ケースには割引率と金額の2つのアサーション不一致があり、失敗テスト数は6件（12件ではない）。入力は顧客C1、日付2026-10-03、在庫あり。

| 固定ケースID | 会員／繁忙期／数量／単価 | 割引率 期待→実際 | 金額 期待→実際 | 根拠 |
| --- | --- | --- | --- | --- |
| SPEC-PREMIUM-PEAK-Q1 | PREMIUM / true / 1 / 100 | 0.05 → 0.10 | 95 → 90 | S03 PREMIUM繁忙期5%、S04 |
| SPEC-PREMIUM-PEAK-Q4 | PREMIUM / true / 4 / 100 | 0.05 → 0.10 | 380 → 360 | S03 PREMIUM繁忙期5%、S04 |
| SPEC-PREMIUM-PEAK-Q5 | PREMIUM / true / 5 / 100 | 0.05 → 0.10 | 475 → 450 | S03 PREMIUM繁忙期5%、S04 |
| SPEC-PREMIUM-PEAK-Q10 | PREMIUM / true / 10 / 100 | 0.05 → 0.10 | 950 → 900 | S03 PREMIUM繁忙期5%、S04 |
| SPEC-STANDARD-OFF-Q5 | STANDARD / false / 5 / 100 | 0.05 → 0.00 | 475 → 500 | S03 STANDARD予約数5以上5%、S04 |
| SPEC-STANDARD-PEAK-Q5 | STANDARD / true / 5 / 100 | 0.05 → 0.00 | 475 → 500 | S03 STANDARD予約数5以上5%、S04 |

PREMIUM繁忙期の仕様上の割引率は5%。STANDARDは予約数5以上なら繁忙期にかかわらず5%。期待値の根拠は指定仕様であり、実装の戻り値に合わせた補正は行っていない。詳細な実装原因分析、問題箇所、実装修正案はPhase3で扱う。

## 6. 制約と未完了

指定範囲の生成・実行・計測を完了。確認したケースについて仕様不足・矛盾は見つからなかった。S02に従い依存先障害、並行予約、永続化、null会員は対象外。S04に従い税・手数料・キャンセル料も対象外。6件のFAILは未修正のまま保持しており、修正や修正後の検証は行っていない。

Git管理されていないため、変更確認は開始時と終了時のSHA-256比較による。Phase1→Phase2を同一チャットで実施しており、独立したAIによる比較実験とは扱わない。公開・デプロイは行っていない。

## 7. 変更確認と配布

- 新規UT：demo-ut/spec/java/com/example/reservation/ReservationServiceSpecTest.java。
- 新規出力：target/spec/20261003-180427/（ログ・実測データ・report.md・確認記録）。作業補助：target/phase2-current-run.txt、target/finish-phase2.ps1。
- samples/phase2/へ今回のreport.md、ログ、surefire-reports、site/jacoco全体、jacoco.exec、ケース対応と確認記録を配布。UTはdemo-ut/spec/javaに保持。既存のPhase2配布物はなく、履歴退避は不要。
- README.mdのPhase2結果・日付・ID・リンクを更新。Phase1・Phase3の結果や以前のtarget出力は保持。
- 本番ソース7ファイル、pom.xml、.gitignore、指定仕様、Phase1 UTと配布物のSHA-256が開始時と一致。[開始時ハッシュ](before-hashes.json) / [変更確認](change-check.json)。target/除外を維持、samples/は除外しない。
- Phase3では今回のUTを再利用する。[再利用確認用ハッシュ](phase3-reuse-hashes.json)。
- [配布検証](distribution-check.json)：コピー先のハッシュ、レポートとHTMLのローカルリンク、コンパイル済みファイル不在、レポート読み返しを確認。

## 8. トークン使用量（概算）

入力 409425、うちキャッシュ済み入力 398080、出力 11319、合計 420744 tokens。推論出力（参考）369。

計測区間（UTC）：2026-10-03 09:03:30.747 ～ 2026-10-03 09:12:00.834。基準値：今回のut -spec指示直前の最後の有効な累計。計測チャット：01a100f5-05c3-7cf2-8750-8d78793bf690。

概算・最終計測時点まで。キャッシュ済み入力は入力の内数であり合計に再加算しない。推論出力は参考値。最終計測後のレポート追記、samplesへのコピー・確認、最終回答、ログ反映待ちの処理は含まれない可能性がある。費用には換算しない。

[累計・差分・時刻と計測制約](token-usage.json)
