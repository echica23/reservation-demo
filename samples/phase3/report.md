# Phase3：仕様レビュー実行レポート

実行日: 2026-10-06（Asia/Tokyo） / 配布用コピー（元の出力: C:\demo\reservation-demo\target\review\20261006-113220-53c9ff）。実行ID: 20261006-113220-53c9ff

## 実行概要

入力: `ut -P "C:\demo\reservation-demo" -review com.example.reservation.ReservationService -D docs/reservation-spec.md`

ルート: C:\demo\reservation-demo。対象はcom.example.reservation.ReservationServiceの公開入口reserve。
参照資料: [業務仕様 S01〜S05](../../docs/reservation-spec.md)、[対象ソース](../../src/main/java/com/example/reservation/ReservationService.java)、[再利用UT](../../demo-ut/spec/java/com/example/reservation/ReservationServiceSpecTest.java)、pom.xml、実行指示書、[Phase2の記録](../phase2/report.md)。

## UTの再利用と仕様対応

Phase2（20261006-112631-e91918）のUTを追加・変更せず再利用。対象実装・仕様・pom・UTのSHA-256がPhase2時点と一致することを確認した。[再利用確認](reuse-check.json) / [UTハッシュ](ut-hashes.json)。Phase1のUTは実行・計測へ混入させていない。

入力検証11件（S01）、割引条件30件（S01/S03/S04）、丸め6件（S04）、過去・未来の日付2件（S01）、在庫不足1件（S02）の計50件。依存はMockito、期待率・金額はケース別固定値、公開入口のみを検証。コメント・固定ID・期待値を保持。

## 実行結果

実行 50件 / 成功 43件 / FAIL 7件 / ERROR 0件 / SKIP 0件。
Phase2と全50件の固定ID・成否・system-out内の入力／期待値／実測が一致。Maven testは終了コード1（テストFAIL）、jacoco:reportは0。コンパイル・環境エラーなし。再試行なし。

実行済みのコマンド（記録用）。再実行する場合は未使用の実行IDへ変更する：

```powershell
& "C:\Users\saram\.m2\wrapper\dists\apache-maven-3.9.9-bin\33b4b2b4\apache-maven-3.9.9\bin\mvn.cmd" "-Pphase3,coverage" "-Ddemo.runId=20261006-113220-53c9ff" test
& "C:\Users\saram\.m2\wrapper\dists\apache-maven-3.9.9-bin\33b4b2b4\apache-maven-3.9.9\bin\mvn.cmd" "-Pphase3,coverage" "-Ddemo.runId=20261006-113220-53c9ff" jacoco:report
```

[テストログ](test.log) / [計測ログ](coverage.log) / [Surefire XML](surefire-reports/TEST-com.example.reservation.ReservationServiceSpecTest.xml) / [全ケース結果](case-results.json) / [集計](summary.json)

## カバレッジ

分母はReservationService単体。Line 33/34（97.06%、missed=1）、Branch 25/26（96.15%、missed=1）。目標Line 100%は未達。

62行目のquantity > 10のtrue側と63行目のreturnが未カバー。先行する入力検証で数量10超を拒否するため、公開入口から到達しない。SPEC-INPUT-QUANTITY-ELEVENは入力拒否を検証して成功しており、15%割引を期待するケースは仕様に存在しない。未カバーと仕様FAILは別に扱う。

[対象クラスHTML](site/jacoco/com.example.reservation/ReservationService.html) / [JaCoCo XML](site/jacoco/jacoco.xml)

## 仕様不一致

全件、顧客C1・日付2026-10-06・単価100・在庫あり。期待率はS03、期待金額はS04に基づく。
| ケースID | 会員 | 繁忙期 | 数量 | 期待率 | 実際率 | 期待金額 | 実際金額 |
| --- | --- | --- | ---: | ---: | ---: | ---: | ---: |
| SPEC-STANDARD-OFF-Q5 | STANDARD | false | 5 | 0.05 | 0.00 | 475 | 500 |
| SPEC-STANDARD-PEAK-Q5 | STANDARD | true | 5 | 0.05 | 0.00 | 475 | 500 |
| SPEC-PREMIUM-PEAK-Q1 | PREMIUM | true | 1 | 0.05 | 0.10 | 95 | 90 |
| SPEC-PREMIUM-PEAK-Q4 | PREMIUM | true | 4 | 0.05 | 0.10 | 380 | 360 |
| SPEC-PREMIUM-PEAK-Q5 | PREMIUM | true | 5 | 0.05 | 0.10 | 475 | 450 |
| SPEC-PREMIUM-PEAK-Q6 | PREMIUM | true | 6 | 0.05 | 0.10 | 570 | 540 |
| SPEC-PREMIUM-PEAK-Q10 | PREMIUM | true | 10 | 0.05 | 0.10 | 950 | 900 |

## 原因と修正案

[review.md](review.md) に、繁忙期判定未使用、数量5の境界、到達不能コードの3指摘を記載。修正案は説明のみで未適用・未検証。正当なFAILを消す期待値変更は行っていない。

## 制約と未完了

依存障害・null会員・並行予約・永続化はS02により対象外。有限のテストで全入力の正しさを保証しない。仕様の未確定事項やUT不備を今回の7件の原因とする根拠はない。独立したAI実験ではなく同じ会話内の再実行・分析。

## 変更確認

本番ソース、仕様、pom、README、実行指示書、Phase1・Phase2 UT、既存samples/phase1・phase2は、ファイル集合とSHA-256が開始前後で一致。新規作成は今回のtarget/review配下とsamples/phase3の成果物。

[開始ハッシュ](before-hashes.json) / [終了ハッシュ](after-hashes.json) / [変更確認](change-check.json)

## トークン使用量（概算）

入力 330681、うちキャッシュ 317056、出力 1869、合計 332550。参考reasoning_output_tokens: 100。
区間: 2026-10-06T02:31:54.151Z 〜 2026-10-06T02:32:44.993Z（ISO 8601）。コマンド境界前の累計ではなく、開始時点で取得できた最新累計を使用。
概算・最終計測時点まで。基準取得前の初期読取りと、最終取得後のレポート作成・コピー・検証・回答を含まない。キャッシュとreasoningは内数であり再加算しない。費用換算なし。
[計測根拠](token-usage.json)。review.mdと二重加算しない。

