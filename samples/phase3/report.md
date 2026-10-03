# Phase3 実行レポート：ReservationService

## 1. 実行概要

- 入力：`ut -P "C:\demo\reservation-demo" -review com.example.reservation.ReservationService -D docs/reservation-spec.md`
- プロジェクト：`C:\demo\reservation-demo`。実際の対象：`com.example.reservation.ReservationService` の1クラス。
- 実行日：2026-10-03（Asia/Tokyo）。実行ID：`20261003-181648`。
- 参照資料：[指定仕様S01～S05](../../docs/reservation-spec.md)、[対象実装](../../src/main/java/com/example/reservation/ReservationService.java)、[Phase2 UT](../../demo-ut/spec/java/com/example/reservation/ReservationServiceSpecTest.java)、pom.xml、今回のログ・Surefire・JaCoCo、今回のデモのPhase2ハッシュとケース実行データ。
- 元出力：`C:\demo\reservation-demo\target\review\20261003-181648`。これは上記出力の配布用コピー。

## 2. 再利用したUT

`demo-ut/spec/java/com/example/reservation/ReservationServiceSpecTest.java` を変更せず再利用。48ケースの固定ID、固定期待値、狙いコメントを保持。Phase3用UTの新規生成・追加なし。Phase1 UTは実行・計測に混ぜていない。コンパイルされたUTがReservationServiceSpecTestだけであることも確認した。

S01入力不正11件、S02在庫なし1件、S03/S04割引の組み合わせと境界26件、S01/S04小数・丸め8件、S01過去・未来日付2件。JUnit5・Mockitoで依存先3サービスをMock化し、公開入口reserveのみを検証する。例外文言、null会員、依存先障害の期待値は追加していない。

Phase2実行ID `20261003-180427` 時点のUT・仕様・本番コード・pom.xmlとのSHA-256一致を確認。[同一性記録](phase2-reuse-check.json)。今回のSurefire各testcaseのsystem-outから48件の一意なIDを読み取り、入力・期待値・実測値・成否がすべてPhase2と同じであることを照合。[全ケース対応](case-results.json)。

## 3. 実行結果

**48件中42件成功、6件失敗、エラー0、スキップ0。** Maven test終了コード1はテストFAILによるもの。コンパイル・環境エラーなし。別途実施したjacoco:reportは終了コード0。

以下は実行時の記録。再実行ではdemo.runIdを未使用IDへ変更し、testとjacoco:reportに同じIDを渡す。testが失敗しても後者を実行する。PowerShell構文解析済み。[コマンド確認](command-check.json)。

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.11'
Set-Location 'C:\demo\reservation-demo'
& "C:\Users\saram\.m2\wrapper\dists\apache-maven-3.9.9-bin\33b4b2b4\apache-maven-3.9.9\bin\mvn.cmd" "-Pphase3,coverage" "-Ddemo.runId=20261003-181648" "-Dmaven.repo.local=C:/Users/saram/.m2/repository" test
& "C:\Users\saram\.m2\wrapper\dists\apache-maven-3.9.9-bin\33b4b2b4\apache-maven-3.9.9\bin\mvn.cmd" "-Pphase3,coverage" "-Ddemo.runId=20261003-181648" "-Dmaven.repo.local=C:/Users/saram/.m2/repository" jacoco:report
```

[実行ログ](test.log) / [Surefire XML](surefire-reports/TEST-com.example.reservation.ReservationServiceSpecTest.xml) / [Surefire概要](surefire-reports/com.example.reservation.ReservationServiceSpecTest.txt) / [JaCoCo生成ログ](coverage.log)

## 4. Coverage

ReservationServiceのXMLカウンタのみを集計。Mock化した依存先、Request/Result、enum、UTを分母に含めない。

| 指標 | covered | missed | 割合 |
| --- | ---: | ---: | ---: |
| Line Coverage | 32 | 0 | 100% |
| Branch Coverage | 24 | 0 | 100% |

Line 100%の目標達成。対象の未カバー行・分岐なし。今回の実行IDで計測した値であり、過去のjacoco.execは使用していない。仕様への適合は別の評価軸である。

[対象クラスHTML](site/jacoco/com.example.reservation/ReservationService.html) / [HTMLトップ](site/jacoco/index.html) / [XML](site/jacoco/jacoco.xml) / [CSV](site/jacoco/jacoco.csv) / [実測データ](jacoco.exec)

## 5. 仕様不一致

顧客C1、予約日2026-10-03、在庫あり。以下6件で割引率と金額が指定仕様に一致しない。各ケース2個のアサーション不一致だが、失敗テスト数は6件。

| 固定ケースID | 会員／繁忙期／数量／単価 | 割引率 期待→実際 | 金額 期待→実際 | 仕様／指摘 |
| --- | --- | --- | --- | --- |
| SPEC-PREMIUM-PEAK-Q1 | PREMIUM / true / 1 / 100 | 0.05 → 0.10 | 95 → 90 | S03 繁忙期5%、S04 / R1 |
| SPEC-PREMIUM-PEAK-Q4 | PREMIUM / true / 4 / 100 | 0.05 → 0.10 | 380 → 360 | S03 繁忙期5%、S04 / R1 |
| SPEC-PREMIUM-PEAK-Q5 | PREMIUM / true / 5 / 100 | 0.05 → 0.10 | 475 → 450 | S03 繁忙期5%、S04 / R1 |
| SPEC-PREMIUM-PEAK-Q10 | PREMIUM / true / 10 / 100 | 0.05 → 0.10 | 950 → 900 | S03 繁忙期5%、S04 / R1 |
| SPEC-STANDARD-OFF-Q5 | STANDARD / false / 5 / 100 | 0.05 → 0.00 | 475 → 500 | S03 5以上5%、S04 / R2 |
| SPEC-STANDARD-PEAK-Q5 | STANDARD / true / 5 / 100 | 0.05 → 0.00 | 475 → 500 | S03 5以上5%、S04 / R2 |

## 6. レビュー

[review.md](review.md) に原因、問題箇所、根拠、確度、固定ケースIDとの対応、修正コード例を記載した。

1. R1：実装62～63行がPREMIUMへ一律10%を返し、繁忙期の5%を反映しない。PREMIUM繁忙期4件に対応。
2. R2：実装65行のquantity > 5が、仕様の5以上という境界と一致しない。STANDARD数量5の2件に対応。

ともに仕様・UT・コード・実測を照合して確認した不一致。金額不一致は誤った割引率から説明できる。修正案は説明とコード例のみで、未適用・未検証。

## 7. 制約と未完了

Phase3の再実行・計測・レビューを完了。6件のFAILは保持。本番修正と修正後の検証は実施していない。仕様対象外の依存先障害、並行予約、永続化、税・手数料・キャンセル料は未評価。確認した失敗ケースには仕様不足・UT不備を示す根拠はない。

同じチャットでPhase1→2→3を実施しており、独立AIによる比較実験ではない。Git管理されていないためSHA-256で変更確認した。公開・デプロイ・既存出力削除は行っていない。

## 8. 変更確認と配布

- 新規：target/review/20261003-181648/ の今回の実行出力、report.md、review.md、各確認記録。
- 作業補助：target/phase3-current-run.txt、target/finish-phase3.ps1、target/phase3-report-template.md、target/phase3-review-template.md。
- 配布：samples/phase3/ にレポート2件、実行ログ、surefire-reports、site/jacoco全体、jacoco.exec、ハッシュ・変更・トークン確認記録。UTはdemo-ut/spec/javaに保持。
- README.md：Phase3の結果・日付・実行ID・リンクだけを更新。他Phaseの結果と過去のtarget出力を保持。
- 本番ソース、pom.xml、.gitignore、仕様、全UT、Phase1/2配布物は開始前後のハッシュ一致。[開始時ハッシュ](before-hashes.json) / [変更確認](change-check.json)。target/除外を維持しsamples/を除外していない。
- 既存Phase3配布物はなかったため履歴退避不要。コピー先のハッシュ、ローカルリンク、コンパイル済みファイル不在、レポート読み返しを[配布検証](distribution-check.json)に記録。

<a id="token-usage"></a>
## 9. トークン使用量（概算）

入力 428032、うちキャッシュ済み入力 418560、出力 5607、合計 433639 tokens。推論出力（参考）116。

計測区間（UTC）：2026-10-03 09:12:44.655 ～ 2026-10-03 09:20:22.290。基準値：今回のut -review指示直前の最後の有効な累計。チャットID：01a100f5-05c3-7cf2-8750-8d78793bf690。

概算・最終計測時点まで。キャッシュ済み入力は入力の内数で合計に再加算しない。推論出力は参考値で合計に加算しない。最終計測後のレポート追記、samplesへのコピー・確認、最終回答、ログ反映待ちの処理は含まれない可能性がある。費用への換算は行わない。

[累計・差分・時刻・制約](token-usage.json)。review.mdも同じ計測を参照するため重複加算しない。

