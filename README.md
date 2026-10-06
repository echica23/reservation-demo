# 予約サービスのJava UTデモ

AIによる単体テスト（UT）の生成・実行・レビューを、同じJavaの予約サービスで段階的に試すデモです。

実装を基準にテストすると何が確認できるのか、業務仕様を基準にすると何が見つかるのかを比較します。特に、**到達不能なコードによる未カバー**と、**実行できても業務仕様に合わない処理によるテストFAIL**を区別して確認します。カバレッジの高さだけでは、業務上の正しさを判断できません。

## デモの概要

対象の `ReservationService.reserve` は、予約の入力を確認し、在庫・会員種別・繁忙期の情報から割引率と支払金額を返します。会員・カレンダー・在庫の3サービスはMockitoでMock化するため、実DBや外部サービスは不要です。

業務仕様はデモ用に用意したものです。実装には仕様との不一致があり、それを検出・説明するところまでを扱います。

| 段階 | AIが行うこと | 確認したいこと |
| --- | --- | --- |
| Phase1：coverage | 実装を読んでUTを生成・実行し、カバレッジを計測 | 実装の経路をどこまで通せるか、通せない箇所にはどんな理由があるか |
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

業務仕様を期待値の根拠にせず、実装の正常系・異常系・分岐を検証します。目標は対象クラスのLine Coverage 100%で、Branch Coverageも別途報告します。ただし、未カバーだけで到達不能とは断定せず、呼び出し経路と条件を解析して理由を説明します。到達不能な処理は未カバーとして残し、privateメソッドの直接呼び出しや本番コードの変更で100%にしません。未カバーは、期待値と実際値の不一致によるテストFAILとは別に報告します。

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

実行IDはAIが未使用のものを選びます。`ut` コマンドは実行後、配布用成果物を `samples/phase1/`〜`samples/phase3/` の該当フォルダへ保存します。既存の配布用成果物は `samples/history/phase<Phase番号>/<保存日時>/` へ退避して照合してから更新します。Mavenの作業用出力と元の結果は `target/` に保持します。各Phaseには、次の成果物を保存します。

- `report.md`：実行条件、UT、テスト結果、カバレッジ、変更確認
- `surefire-reports/`：テスト結果の詳細
- `site/jacoco/index.html`・`jacoco.xml`：カバレッジレポート
- `jacoco.exec`：カバレッジの実行データ
- 実行ログ
- `review.md`：Phase3の原因分析と修正案

各Phaseの実行結果は `samples/phase1/report.md`〜`samples/phase3/report.md`、原因と修正案は `samples/phase3/review.md` を参照してください。実行日・実行ID・件数・カバレッジは各レポートに記録し、READMEには転載しません。

JaCoCoレポート全体には他の本番クラスも含まれます。デモの評価対象は `ReservationService` 単体です。

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

テストが失敗しても、続く `jacoco:report` を別に実行してください。同じ実行IDを使うことで、その実行のカバレッジを生成できます。Maven単体では、AIが作成する `report.md` や `review.md` の生成、`samples/` の更新は行われません。


