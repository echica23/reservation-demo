# 予約サービスのJava UTデモ

AIによる単体テスト（UT）の生成・実行・レビューを、同じJavaの予約サービスで段階的に試すデモです。

実装を基準にテストすると何が確認できるのか、業務仕様を基準にすると何が見つかるのかを比較します。特に、**カバレッジが100%でも、業務仕様に合っているとは限らない**ことを、実際のテスト結果で示します。

## デモの概要

対象の `ReservationService.reserve` は、予約の入力を確認し、在庫・会員種別・繁忙期の情報から割引率と支払金額を返します。会員・カレンダー・在庫の3サービスはMockitoでMock化するため、実DBや外部サービスは不要です。

業務仕様はデモ用に用意したものです。実装には仕様との不一致があり、それを検出・説明するところまでを扱います。

| 段階 | AIが行うこと | 確認したいこと |
| --- | --- | --- |
| Phase1：coverage | 実装を読んでUTを生成・実行し、カバレッジを計測 | 実装の経路をどこまで通せるか |
| Phase2：spec | 業務仕様から別のUTを生成・実行し、カバレッジも計測 | 仕様どおりの結果を返すか |
| Phase3：review | Phase2のUTをそのまま再実行し、仕様・実装・結果を照合 | なぜ失敗したか、どこをどう直すべきか |

全Phaseで同じ本番ソースとビルド設定を使います。Phase1とPhase2のUTは分離し、Phase3ではUTを追加・変更しません。本番コードの修正は行わず、修正案をレビューに残します。

## 必要な環境

- Java 21
- Maven 3.9系
- ローカルファイルの読み書きとMaven実行ができるAI作業環境（Codexなど）

JUnit5・Mockito・JaCoCoとPhase別のMavenプロファイルは `pom.xml` に設定済みです。初回は依存ライブラリの取得にネットワーク接続が必要になる場合があります。VSCodeなどのエディタは任意です。

## 使い方

### 1. AIに実行指示を読み込ませる

プロジェクトを作業フォルダとして開き、AIのチャットへ次のように入力します。パスは配置先に合わせて変更してください。

```text
C:\demo\reservation-demo\prompts\ut-instructions.md を読み、
以後のutコマンドに従ってください。まだ実行せず待機してください。
```

MavenがPATHにない場合は、利用する `mvn.cmd` の場所も伝えてください。このサンプルの実行環境では、`C:\Users\saram\.m2\wrapper\dists` 以下の保存済みMavenを利用しました。このプロジェクト自体にMaven Wrapperは含まれていません。

### 2. Phase1：実装を基準にテストする

```text
ut -P "C:\demo\reservation-demo" -coverage com.example.reservation.ReservationService
```

業務仕様を期待値の根拠にせず、実装の正常系・異常系・分岐を検証します。目標は対象クラスのLine Coverage 100%で、Branch Coverageも別途報告します。

### 3. Phase2：仕様を基準にテストする

```text
ut -P "C:\demo\reservation-demo" -spec com.example.reservation.ReservationService -D docs/reservation-spec.md
```

[業務仕様](docs/reservation-spec.md)を正として、新しいUTを作成します。各UTには狙いと仕様の対応をコメントで記載します。仕様との不一致による失敗は、期待値を実装に合わせず、そのまま結果として残します。

### 4. Phase3：失敗原因と修正案を確認する

```text
ut -P "C:\demo\reservation-demo" -review com.example.reservation.ReservationService -D docs/reservation-spec.md
```

Phase2のUTを再利用し、失敗ケースと問題箇所を対応付けます。原因、仕様上の根拠、修正案を `review.md` にまとめます。修正案の適用や修正後の検証は、このコマンドの対象外です。

### コマンドの注意点

- `ut` はAIへの自然言語コマンドです。ターミナルで実行するCLIではありません。
- `-P` はプロジェクトルートの絶対パスです。各コマンドで同じルートを指定します。
- `-D` の相対パスはプロジェクトルートを基準に解決します。
- 同じチャットでPhase1→Phase2→Phase3の順に進めます。独立したAIによる比較実験ではありません。
- 生成UTを含む状態からやり直す場合は、既存UTの扱いをAIに明示してください。実行指示では、予期しない既存UTを上書き・削除せず、扱いを確認するよう定めています。

## ファイル構成と成果物

| 場所 | 内容 |
| --- | --- |
| `samples/phase1/`〜`samples/phase3/` | 配布用に保存した各Phaseの実行結果 |
| `src/main/java/` | 全Phase共通の本番ソース |
| `docs/reservation-spec.md` | Phase2・3で使用する業務仕様 |
| `prompts/ut-instructions.md` | AIに読み込ませる実行ルール |
| `demo-ut/coverage/java/` | Phase1で生成したUT |
| `demo-ut/spec/java/` | Phase2で生成し、Phase3で再利用するUT |
| `target/coverage/<実行ID>/` | Phase1の結果 |
| `target/spec/<実行ID>/` | Phase2の結果 |
| `target/review/<実行ID>/` | Phase3の結果とレビュー |

実行IDはAIが未使用のものを選びます。`ut` コマンドは実行後、配布用成果物を `samples/phase1/`〜`samples/phase3/` の該当フォルダへ保存し、READMEの該当Phaseの結果・リンクも更新します。既存の配布用成果物は `samples/history/phase<Phase番号>/<保存日時>/` へ退避して照合してから更新します。Mavenの作業用出力と元の結果は `target/` に保持します。各Phaseには、次の成果物を保存します。

- `report.md`：実行条件、UT、テスト結果、カバレッジ、変更確認
- `surefire-reports/`：テスト結果の詳細
- `site/jacoco/index.html`・`jacoco.xml`：カバレッジレポート
- `jacoco.exec`：カバレッジの実行データ
- 実行ログ
- `review.md`：Phase3の原因分析と修正案

JaCoCoレポート全体には他の本番クラスも含まれます。デモの評価対象は `ReservationService` 単体です。

## サンプル結果

2026年10月3日に、このフォルダで実行した結果です。生成UTは `demo-ut/`、配布用の実行成果物は `samples/phase1/`〜`samples/phase3/` に保存しています。テスト件数や結果は、この実行で生成したUTによる実測値であり、再生成時に同じ件数・結果を強制するものではありません。

| Phase | 実行件数 | 成功 | 失敗 | Line Coverage | Branch Coverage | レポート |
| --- | ---: | ---: | ---: | --- | --- | --- |
全Phaseでエラー・スキップは0件です。本番ソースと `pom.xml` が未変更であることをSHA-256で確認しました。

Phase2・3では、STANDARDの数量5で割引が適用されない問題（2ケース）と、PREMIUMの繁忙期に10%割引が適用される問題（5ケース）を検出しました。Phase3では、それぞれ数量境界の比較条件と繁忙期判定の欠落に原因を特定しています。修正案は未適用・未検証です。

**読みどころは、全Phaseでカバレッジが100%でも、仕様を基準にしたPhase2・3では失敗が見つかる点です。**

配布用の `samples/` にはレポート、実行ログ、Surefire結果、JaCoCoのHTML/XML/CSVと実行データ、変更確認用のハッシュ記録を同梱しています。コンパイル済みファイルは含めていません。元の実行結果は `target/` に残し、Gitの除外を維持しています。`ut` コマンドで再実行すると、AIが履歴を保持して該当Phaseの配布用成果物を更新します。ログやハッシュ記録には実行当時のローカルパスが含まれます。

Phase1 実測（2026-10-03、実行ID：`20261003-175803`）：35件成功、失敗・エラー・スキップ0。ReservationService のLine Coverage 32/32（100%）、Branch Coverage 24/24（100%）。[実行レポート](samples/phase1/report.md) / [対象クラスcoverage](samples/phase1/site/jacoco/com.example.reservation/ReservationService.html)。業務仕様への適合はPhase1では判定しない。

Phase2 実測（2026-10-03、実行ID：`20261003-180427`）：48件中42件成功、6件失敗、エラー・スキップ0。ReservationService のLine Coverage 32/32（100%）、Branch Coverage 24/24（100%）。[仕様基準UTレポート](samples/phase2/report.md) / [対象クラスcoverage](samples/phase2/site/jacoco/com.example.reservation/ReservationService.html)。PREMIUM繁忙期とSTANDARD予約数5で仕様不一致を検出。

Phase3 実測（2026-10-03、実行ID：`20261003-181648`）：Phase2 UTを変更せず再利用し、48件中42件成功・6件失敗、エラー・スキップ0。Line 32/32（100%）、Branch 24/24（100%）。[実行レポート](samples/phase3/report.md) / [原因・修正案レビュー](samples/phase3/review.md) / [対象クラスcoverage](samples/phase3/site/jacoco/com.example.reservation/ReservationService.html)。繁忙期判定の欠落と数量境界の不一致を確認。修正案は未適用・未検証。

## 生成済みUTをMavenで再実行する

AIによる生成を行わず、既存UTだけを実行する場合は、プロジェクトルートで次を実行します。毎回、未使用の実行IDを指定してください。

```text
mvn "-Pphase1,coverage" "-Ddemo.runId=manual-phase1-001" test
mvn "-Pphase1,coverage" "-Ddemo.runId=manual-phase1-001" jacoco:report

mvn "-Pphase2,coverage" "-Ddemo.runId=manual-phase2-001" test
mvn "-Pphase2,coverage" "-Ddemo.runId=manual-phase2-001" jacoco:report

mvn "-Pphase3,coverage" "-Ddemo.runId=manual-phase3-001" test
mvn "-Pphase3,coverage" "-Ddemo.runId=manual-phase3-001" jacoco:report
```

Mavenの `-P` はプロファイル指定です。AI向け `ut` コマンドのプロジェクトルート指定とは異なります。

PowerShellではカンマを含む引数の解釈を避けるため、上記のように `"-Pphase1,coverage"` と `"-Ddemo.runId=manual-phase1-001"` を引用符で囲んでください。レポート生成のゴール名は `jacoco:report` です。`mvn` がPATHにない場合は、保存済みのMavenを呼び出します（パスは利用環境に合わせて変更）。

```powershell
& "C:\Users\saram\.m2\wrapper\dists\apache-maven-3.9.9-bin\33b4b2b4\apache-maven-3.9.9\bin\mvn.cmd" "-Pphase1,coverage" "-Ddemo.runId=manual-phase1-001" jacoco:report
```

テストが失敗しても、続く `jacoco:report` を別に実行してください。同じ実行IDを使うことで、その実行のカバレッジを生成できます。Maven単体では、AIが作成する `report.md` や `review.md` の生成、`samples/` やREADMEの更新は行われません。


