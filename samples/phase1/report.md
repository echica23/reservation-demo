配布用コピー。元実行ID：20261006-111406。元出力：C:\demo\reservation-demo\target\coverage\20261006-111406。

# Phase1 UT・カバレッジ実行レポート

## 実行概要

- 実行日：2026-10-06（Asia/Tokyo）。Phase1：実装基準。
- 入力：`ut -P "C:\demo\reservation-demo" -coverage com.example.reservation.ReservationService`
- プロジェクトルート：`C:\demo\reservation-demo`
- 解決した対象一覧：`com.example.reservation.ReservationService`（1クラス）
- 最終実行ID：`20261006-111406`
- 元出力：`C:\demo\reservation-demo\target\coverage\20261006-111406`
- 参照：対象実装、同パッケージの3依存インターフェース・2レコード・MemberType、pom.xml、ut-instructions.md、今回のSurefire・JaCoCo結果。業務仕様、Phase2 UT、過去レビューは未参照。

## 生成・利用したUT

[ReservationServiceTest.java](../../demo-ut/coverage/java/com/example/reservation/ReservationServiceTest.java)を新規作成。Phase1 UTのみ実行。既存UTは作業ツリーに存在しなかった。Gitでは過去ファイルの削除として表示されていたが、復元・参照していない。Phase2 UTは生成していない。

JUnit5とMockitoで3依存先をMock化。公開メソッドreserve経由で検証し、privateへの直接アクセスなし。期待値は実装の観測可能な挙動に基づく固定値。

- 入力検証11件：request null、顧客ID null/空/空白、日付null、数量0/-1/11、価格null/0/負数。例外種別・メッセージ、依存先未呼び出しを確認。
- 料金20件：NONE/STANDARD/PREMIUM/null会員、数量1/5/6/10、繁忙期有無、HALF_UP境界、数量・割引適用後の丸め、正の微小価格から0への丸め。
- 在庫なし1件：例外と顧客・カレンダー未呼び出しを確認。

UTのケース表には固定ID、入力、固定の期待割引率・金額を記載。固定IDは表示名、アサーション、Surefire XMLの各testcase内system-outにも記録。[ケース結果対応表](case-results.json)の32件は一意で、実行順番号に依存しない。

## 実行結果

| 実行 | 成功 | 失敗 | エラー | スキップ |
| ---: | ---: | ---: | ---: | ---: |
| 32 | 32 | 0 | 0 | 0 |

Java 21.0.11、Maven 3.9.9、JUnit 5.11.4、Mockito 4.11.0、JaCoCo 0.8.12。テストとcoverage生成の終了コードはいずれも0。既存pom.xmlの設定を使用。

プロジェクトルートで実行した当時のコマンド：

```powershell
& "C:\Users\saram\.m2\wrapper\dists\apache-maven-3.9.9-bin\33b4b2b4\apache-maven-3.9.9\bin\mvn.cmd" "-Pphase1,coverage" "-Ddemo.runId=20261006-111406" test
& "C:\Users\saram\.m2\wrapper\dists\apache-maven-3.9.9-bin\33b4b2b4\apache-maven-3.9.9\bin\mvn.cmd" "-Pphase1,coverage" "-Ddemo.runId=20261006-111406" jacoco:report
```

これは実行記録。再実行にはdemo.runIdを未使用のIDに変更すること。[PowerShell構文確認](command-check.json)済み。構文確認のためだけのテスト再実行は行っていない。

[テストログ](test.log)、[coverageログ](coverage.log)、[Surefire XML](surefire-reports/TEST-com.example.reservation.ReservationServiceTest.xml)、[Surefire要約](surefire-reports/com.example.reservation.ReservationServiceTest.txt)。

初回20261006-111220も32件成功。Surefire結果に固定IDを残す標準出力を追加し、新IDで再実行した。初回結果はtargetに保持。

## カバレッジ

JaCoCo XMLのReservationServiceクラスcounterのみを集計。依存先、DTO、enum、UTを分母に含めない。HTMLトップのプロジェクト集計と区別する。

| 指標 | covered | missed | 合計 | 割合 |
| --- | ---: | ---: | ---: | ---: |
| Line Coverage | 33 | 1 | 34 | 97.06% |
| Branch Coverage | 25 | 1 | 26 | 96.15% |

Line Coverage 100%は未達。ReservationService.javaの63行目 `return new BigDecimal("0.15");` が未実行。62行目 `quantity > 10` のtrue側が未カバー。validateが数量11以上を拒否するため、公開入口から到達できない。数量11の拒否はUTで確認済み。実装変更、リフレクション、除外設定、対象縮小は行っていない。

[対象クラスHTML](site/jacoco/com.example.reservation/ReservationService.html)、[ソース行別HTML](site/jacoco/com.example.reservation/ReservationService.java.html)、[XML](site/jacoco/jacoco.xml)、[CSV](site/jacoco/jacoco.csv)、[計測データ](jacoco.exec)。

## 制約と未完了

実行・計測完了。Line Coverage目標は到達不能行により未達。業務仕様への適合は評価していない。PASSと高いcoverageだけで仕様適合とは結論付けない。Phase2の仕様不一致判定、Phase3レビューは今回対象外。

通常の実行環境が起動に失敗したため、承認された権限で実行した。既存Mavenを使用し、インストールやビルド設定変更なし。

## 変更確認・配布

[変更前SHA256](before-hashes.json)と[変更照合](change-check.json)で、開始時点の13ファイルが一致。production code、pom.xml、README、プロンプト、仕様資料、.gitignoreは未変更。開始前のGit変更を保持。

今回追加・更新したもの：上記UT 1ファイル、target/coverageの2実行分の成果物、target内の作業補助ファイル、samples/phase1の配布用成果物。既存samplesフォルダは開始時に存在せず、退避対象なし。Phase2・Phase3の成果物は変更していない。Git公開なし。

配布にはレポート、実行ログ、終了コード、Surefire、site/jacoco全体、jacoco.exec、ハッシュ・ケースID・コマンド・トークン記録を含む。classes/test-classes等は配布しない。[配布検証](distribution-check.json)にハッシュ・リンク・コンパイル済みファイル非混入の結果を保存。

## トークン使用量（概算）

概算・最終計測時点まで。対象チャット：01a10ef8-e498-7c81-ba82-7b36363b6867。実行ID：20261006-111406。基準値は今回のut指示直前の最後の有効な累計。

| 項目 | 差分 |
| --- | ---: |
| 入力 | 493985 |
| うちキャッシュ済み入力（内数） | 477952 |
| 出力 | 13524 |
| 合計 | 507509 |
| 推論出力（参考値） | 429 |

計測区間：10/06/2026 02:11:48 ～ 10/06/2026 02:17:53（UTC）。[数値・時刻・差分の根拠](token-usage.json)。キャッシュ済み入力・推論出力を合計へ再加算しない。最終計測後のレポート追記、samplesコピー・確認、最終回答、ログ反映待ちは含まれない可能性があり、厳密な全作業使用量ではない。費用換算なし。
