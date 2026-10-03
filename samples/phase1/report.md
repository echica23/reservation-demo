# Phase1 UT実行レポート：ReservationService

## 1. 実行概要

- 入力：`ut -P "C:\demo\reservation-demo" -coverage com.example.reservation.ReservationService`
- プロジェクト：`C:\demo\reservation-demo`
- 実行日：2026-10-03（Asia/Tokyo）、最終実行ID：`20261003-175803`
- 実際の対象：`com.example.reservation.ReservationService` の1クラス。
- 参照：対象実装、Request/Result、MemberType、依存先3インターフェース、pom.xml、.gitignore、今回のUT・実行結果・JaCoCo。
- 業務仕様、Phase2 UT、既存レビュー、READMEのサンプル結果、過去の成果物は生成・評価の根拠にしていない。READMEの見出しだけを確認し、結果更新は機械的に実施。
- 元の出力：`C:\demo\reservation-demo\target\coverage\20261003-175803`
- このファイルは上記実行出力から作成した配布用コピー。

## 2. 生成したUT

[ReservationServiceTest.java](../../demo-ut/coverage/java/com/example/reservation/ReservationServiceTest.java) を新規生成。JUnit5とMockitoを使用し、依存先3サービスをMock化。公開入口 reserve のみから検証し、private methodの直接呼出しは行わない。

既存UTは開始時に存在しなかった。Phase1専用の demo-ut/coverage/java/ を利用。Phase2/3 UTは生成・実行していない。割引率・金額の期待値は実装の観測可能な挙動に基づくケースごとの固定値。共通の有効入力は顧客C1・日付2026-10-03。

パラメータの固定IDを表示名・アサーションメッセージに含めた。SurefireのXMLは表示名を省略するため、各 testcase の system-out に固定IDと入力・期待値を記録した。35件すべてのIDが一意で、実際のXMLから対応を確認済み。[対応データ](case-results.json)。

| 固定ケースID | 入力・固定期待値／検証内容 | 結果 |
| --- | --- | --- |
| INVENTORY-UNAVAILABLE | customer=C1  /  date=2026-10-03  /  quantity=1  /  unitPrice=100  /  available=false | PASS |
| INPUT-REQUEST-NULL | input=null  /  expectedException=Request is required | PASS |
| INPUT-CUSTOMER-NULL | input=ReservationRequest[customerId=null, date=2026-10-03, quantity=1, unitPrice=100]  /  expectedException=Customer ID is required | PASS |
| INPUT-CUSTOMER-EMPTY | input=ReservationRequest[customerId=, date=2026-10-03, quantity=1, unitPrice=100]  /  expectedException=Customer ID is required | PASS |
| INPUT-CUSTOMER-BLANK | input=ReservationRequest[customerId= \t\n, date=2026-10-03, quantity=1, unitPrice=100]  /  expectedException=Customer ID is required | PASS |
| INPUT-DATE-NULL | input=ReservationRequest[customerId=C1, date=null, quantity=1, unitPrice=100]  /  expectedException=Date is required | PASS |
| INPUT-QUANTITY-ZERO | input=ReservationRequest[customerId=C1, date=2026-10-03, quantity=0, unitPrice=100]  /  expectedException=Quantity must be between 1 and 10 | PASS |
| INPUT-QUANTITY-NEGATIVE | input=ReservationRequest[customerId=C1, date=2026-10-03, quantity=-1, unitPrice=100]  /  expectedException=Quantity must be between 1 and 10 | PASS |
| INPUT-QUANTITY-ELEVEN | input=ReservationRequest[customerId=C1, date=2026-10-03, quantity=11, unitPrice=100]  /  expectedException=Quantity must be between 1 and 10 | PASS |
| INPUT-PRICE-NULL | input=ReservationRequest[customerId=C1, date=2026-10-03, quantity=1, unitPrice=null]  /  expectedException=Unit price must be positive | PASS |
| INPUT-PRICE-ZERO | input=ReservationRequest[customerId=C1, date=2026-10-03, quantity=1, unitPrice=0]  /  expectedException=Unit price must be positive | PASS |
| INPUT-PRICE-NEGATIVE | input=ReservationRequest[customerId=C1, date=2026-10-03, quantity=1, unitPrice=-0.01]  /  expectedException=Unit price must be positive | PASS |
| DISCOUNT-PREMIUM-OFF-Q1 | member=PREMIUM  /  peak=false  /  quantity=1  /  unitPrice=100  /  expectedRate=0.10  /  expectedTotal=90 | PASS |
| DISCOUNT-PREMIUM-PEAK-Q1 | member=PREMIUM  /  peak=true  /  quantity=1  /  unitPrice=100  /  expectedRate=0.10  /  expectedTotal=90 | PASS |
| DISCOUNT-PREMIUM-OFF-Q10 | member=PREMIUM  /  peak=false  /  quantity=10  /  unitPrice=100  /  expectedRate=0.10  /  expectedTotal=900 | PASS |
| DISCOUNT-PREMIUM-PEAK-Q10 | member=PREMIUM  /  peak=true  /  quantity=10  /  unitPrice=100  /  expectedRate=0.10  /  expectedTotal=900 | PASS |
| DISCOUNT-STANDARD-OFF-Q1 | member=STANDARD  /  peak=false  /  quantity=1  /  unitPrice=100  /  expectedRate=0.00  /  expectedTotal=100 | PASS |
| DISCOUNT-STANDARD-OFF-Q5 | member=STANDARD  /  peak=false  /  quantity=5  /  unitPrice=100  /  expectedRate=0.00  /  expectedTotal=500 | PASS |
| DISCOUNT-STANDARD-PEAK-Q5 | member=STANDARD  /  peak=true  /  quantity=5  /  unitPrice=100  /  expectedRate=0.00  /  expectedTotal=500 | PASS |
| DISCOUNT-STANDARD-OFF-Q6 | member=STANDARD  /  peak=false  /  quantity=6  /  unitPrice=100  /  expectedRate=0.05  /  expectedTotal=570 | PASS |
| DISCOUNT-STANDARD-PEAK-Q6 | member=STANDARD  /  peak=true  /  quantity=6  /  unitPrice=100  /  expectedRate=0.05  /  expectedTotal=570 | PASS |
| DISCOUNT-STANDARD-OFF-Q10 | member=STANDARD  /  peak=false  /  quantity=10  /  unitPrice=100  /  expectedRate=0.05  /  expectedTotal=950 | PASS |
| DISCOUNT-NONE-OFF-Q1 | member=NONE  /  peak=false  /  quantity=1  /  unitPrice=100  /  expectedRate=0.00  /  expectedTotal=100 | PASS |
| DISCOUNT-NONE-PEAK-Q10 | member=NONE  /  peak=true  /  quantity=10  /  unitPrice=100  /  expectedRate=0.00  /  expectedTotal=1000 | PASS |
| DISCOUNT-MEMBER-NULL | member=null  /  peak=false  /  quantity=6  /  unitPrice=100  /  expectedRate=0.00  /  expectedTotal=600 | PASS |
| ROUND-NONE-BELOW-HALF | member=NONE  /  peak=false  /  quantity=1  /  unitPrice=100.49  /  expectedRate=0.00  /  expectedTotal=100 | PASS |
| ROUND-NONE-HALF | member=NONE  /  peak=false  /  quantity=1  /  unitPrice=100.50  /  expectedRate=0.00  /  expectedTotal=101 | PASS |
| ROUND-NONE-ABOVE-HALF | member=NONE  /  peak=false  /  quantity=1  /  unitPrice=100.51  /  expectedRate=0.00  /  expectedTotal=101 | PASS |
| ROUND-PREMIUM-HALF | member=PREMIUM  /  peak=false  /  quantity=1  /  unitPrice=105  /  expectedRate=0.10  /  expectedTotal=95 | PASS |
| ROUND-STANDARD-HALF | member=STANDARD  /  peak=false  /  quantity=6  /  unitPrice=105  /  expectedRate=0.05  /  expectedTotal=599 | PASS |
| ROUND-AFTER-QUANTITY | member=NONE  /  peak=false  /  quantity=3  /  unitPrice=0.49  /  expectedRate=0.00  /  expectedTotal=1 | PASS |
| PRICE-POSITIVE-SMALL | member=NONE  /  peak=false  /  quantity=1  /  unitPrice=0.01  /  expectedRate=0.00  /  expectedTotal=0 | PASS |
| DEPENDENCY-INVENTORY-THROWS | customer=C1  /  date=2026-10-03  /  quantity=1  /  unitPrice=100  /  failing=inventory | PASS |
| DEPENDENCY-CUSTOMER-THROWS | customer=C1  /  date=2026-10-03  /  quantity=1  /  unitPrice=100  /  failing=customer | PASS |
| DEPENDENCY-CALENDAR-THROWS | customer=C1  /  date=2026-10-03  /  quantity=1  /  unitPrice=100  /  failing=calendar | PASS |

## 3. 実行結果

35件実行、成功35、失敗0、エラー0、スキップ0。test と jacoco:report は終了コード0。

最初の試行は既定ローカルリポジトリが C:\.m2\repository となりテスト開始前に環境エラー。既存の C:\Users\saram\.m2\repository をコマンド引数で指定して解消。初回実行ID 20261003-175205 の出力は保持。その実行は35件成功・Line 32/32・Branch 24/24。ID記録を補った後、別の未使用IDで最終検証を実施した。環境エラーをテストFAILや実装不備とは扱わない。

以下は最終実行時の記録。再実行するときは demo.runId を未使用IDへ変更し、test と jacoco:report に同じIDを指定する。PowerShell構文解析済み。[構文確認](command-check.json)。

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.11'
Set-Location 'C:\demo\reservation-demo'
& "C:\Users\saram\.m2\wrapper\dists\apache-maven-3.9.9-bin\33b4b2b4\apache-maven-3.9.9\bin\mvn.cmd" "-Pphase1,coverage" "-Ddemo.runId=20261003-175803" "-Dmaven.repo.local=C:/Users/saram/.m2/repository" test
& "C:\Users\saram\.m2\wrapper\dists\apache-maven-3.9.9-bin\33b4b2b4\apache-maven-3.9.9\bin\mvn.cmd" "-Pphase1,coverage" "-Ddemo.runId=20261003-175803" "-Dmaven.repo.local=C:/Users/saram/.m2/repository" jacoco:report
```

[実行ログ](test.log) / [Surefire XML](surefire-reports/TEST-com.example.reservation.ReservationServiceTest.xml) / [Surefire概要](surefire-reports/com.example.reservation.ReservationServiceTest.txt) / [計測レポート生成ログ](coverage.log)

## 4. Coverage

対象クラスのJaCoCo XMLカウンタを使用。依存先、レコード、enum、UTは以下の分母に含めない。HTMLのプロジェクト全体集計とは区別する。

| 指標 | covered | missed | 割合 |
| --- | ---: | ---: | ---: |
| Line Coverage | 32 | 0 | 100% |
| Branch Coverage | 24 | 0 | 100% |

Line 100%の目標達成。対象クラスに未カバー行・分岐なし。除外設定追加、対象縮小、production codeの変更なし。

[対象クラスHTML](site/jacoco/com.example.reservation/ReservationService.html) / [HTMLトップ](site/jacoco/index.html) / [XML](site/jacoco/jacoco.xml) / [CSV](site/jacoco/jacoco.csv) / [実測データ](jacoco.exec)

## 5. 制約と未完了

Phase1は実装の挙動を確認したもの。PASSとcoverage 100%から業務仕様への適合は結論付けない。仕様との不一致判定・原因レビューは未実施で、Phase2/3の範囲。Gitリポジトリではないため変更確認は開始時と終了時のSHA-256比較で実施した。公開・デプロイは行わない。

## 6. 変更確認と配布

- 新規UT：demo-ut/coverage/java/com/example/reservation/ReservationServiceTest.java
- 新規出力：target/coverage/20261003-175205/ と target/coverage/20261003-175803/（ログ、計測、レポート、確認記録）。作業補助：target/phase1-current-run.txt、target/finish-phase1.ps1。
- 配布：samples/phase1/ に最終実行のreport.md、ログ、Surefire、site/jacoco全体、jacoco.exec、ケース対応、トークン・ハッシュ・変更確認記録。既存のsamples/phase1はなかったため履歴退避不要。
- README.md：Phase1の今回の結果・日付・ID・リンクを更新。他Phaseの結果は保持。
- 本番ソース7ファイル、pom.xml、.gitignore のSHA-256が開始前と一致。[開始前ハッシュ](before-hashes.json) / [変更確認](change-check.json)。target/除外を維持しsamples/は除外していない。
- 配布確認は [distribution-check.json](distribution-check.json) に記録。レポート以外のコピーのハッシュ、ローカルリンク、コンパイル済みファイル不在を確認する。

## 7. トークン使用量（概算）

入力 813202、うちキャッシュ済み入力 765184、出力 13114、合計 826316 tokens。推論出力（参考）684。

計測区間（UTC）：10/03/2026 08:50:39 ～ 10/03/2026 09:02:37。チャットID：01a100f5-05c3-7cf2-8750-8d78793bf690。基準値：ut指示より前の最後の有効な累計。

概算・最終計測時点まで。キャッシュ済み入力は入力の内数。推論出力は参考値で合計に再加算しない。最終計測後のレポート追記、samplesへのコピー・確認、最終回答、ログ反映待ちの処理を含まない可能性がある。費用換算は行わない。

[数値・時刻・差分・制約](token-usage.json)
