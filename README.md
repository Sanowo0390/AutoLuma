# AutoLuma

![Build](https://github.com/Sanowo0390/AutoLuma/actions/workflows/build.yml/badge.svg)

**Minecraft Java Edition 26.3 / Fabric — 自動明るさ補正Mod**

暗所で補正強度をなめらかに上げ、明るい場所に戻ると自然に下げます。サーバーの照明、ポーション効果、Minecraftの保存済みガンマ設定は変更しません。
### ⚠️このプラグインはAIのみで作られました⚠️

> **v1.1.5（試験版）** — Javaコンパイル・単体テスト・GLSL構文検証はGitHub Actionsで実行します。Minecraft実機でのVitrail/Iris/Complementaryとの互換性は未検証です。

## 動作方式

| 描画方式 | 仕組み |
| --- | --- |
| **自動（推奨）** | 暗視相当の補正。IrisとVitrailではシェーダーパックの暗視強度・明るさのUniformにも値を渡す |
| 暗視補正 | バニラのライトマップと対応シェーダー向け暗視強度を補正 |
| ガンマ補正 | Minecraftのライトマップに一時的な明るさを加算し、対応シェーダーの`screenBrightness`も補正 |
| 画面補正 | 全画面ポストエフェクトによる暗部補正。試験的な実装で、Vitrail導入時は使用しない |

### シェーダー互換性

**Iris 26.3：** Irisの`CommonUniforms.getNightVision()`と、`screenBrightness`用のUniform supplierに対応するオプションのMixinを追加しました。自動モードで以前の全画面ポストエフェクトを選択せず、暗視補正を使います。

**Vitrail 0.13.0-beta：** Vitrailの独自`FrameState.nightVision()`および`screenBrightness()`を補正します。Vulkanパイプライン生成中のネイティブクラッシュが報告されているため、Vitrailでの全画面ポストエフェクトを無効化しています。

**重要：** いずれの方法もシェーダーパックが該当Uniformを使うことが前提です。Complementaryなどが期待した明るさになるとは限りません。完全な互換性やFullbright効果は保証していません。Iris/Vitrailを使用せずに動作する方式と、シェーダーの照明アルゴリズムには差があります。

## BadOptimizationsとの互換性（v1.1.5）

BadOptimizations 26.3が公開している `badoptimizations:cache_hooks` を使い、
AutoLuma専用のライトマップ更新フックを自動登録します。
AutoLumaの実際のガンマ補正値・暗視補正値が変化したとき、または
ワールドが切り替わったときだけ、BadOptimizationsにライトマップ再計算を要求します。
補正値が一定なら従来どおりキャッシュされます。

**BadOptimizationsの推奨設定：**

```yaml
enable_lightmap_caching: true
ignore_mod_incompatibilities: false
ignore_mod_cache_hooks: false
```

設定の変更とMod更新後はMinecraftを再起動してください。
BadOptimizationsが未インストールでもAutoLumaは動作し、
BadOptimizationsを必須依存としていません。
この対応が解決するのはライトマップ更新の省略に起因する遅延です。
Iris/Vitrail/Complementary Shadersの最終的な画面補正の可否は
別の描画経路にも左右され、実機では未検証です。

## 設定

Mod Menu → AutoLuma → 設定。

- 自動明るさ補正：オン/オフ
- 常時適用：暗所判定にかかわらず補正を適用
- 描画方式：自動 / 画面補正 / 暗視補正 / ガンマ補正
- 補正強度：0–100%
- 暗所判定：光レベル0–14
- 切替のなめらかさ：1–100 tick

小さな表示には光量、現在の補正強度、描画環境、Iris/VitrailのUniform読み取り回数が表示されます。**読み取り回数が増えることは、GPU上で目に見える補正が完了した証拠ではありません。**

設定ファイルは`config/autoluma.json`。v1.0.x形式は初回読み込み時に移行します。

## 動作確認

1. Minecraft 26.3 + Fabric API + Mod Menu + AutoLumaを導入。
2. VitrailまたはIrisでシェーダーパックを有効にし、AutoLumaの画面で描画環境名を確認。
3. **自動 / 補正強度100% / 常時適用オン**で変化を比較。
4. 「Uniform読み取り」回数が増えない場合は、実行中のシェーダーが該当値を読んでいない可能性、またはMixinが適用されていない可能性があります。
5. 回数が増えるのに明るさが変わらない場合は、シェーダーパック側の照明・暗視ロジックが値を使用していない可能性があります。
6. 切り分けのため、Iris/Vitrailを使わない構成でも比較してください。

過去にVitrailで`vkCreateGraphicsPipelines`中の`EXCEPTION_ACCESS_VIOLATION`が報告されています。安全確認が取れるまで、Vitrail環境で画面補正を強制的に有効化しないでください。

## ビルド

必要：JDK 25 / Gradle 9.7.1

```bash
gradle clean build
```

Windows用の`build.ps1`もあります。成功すると`build/libs/autoluma-1.1.4.jar`が生成されます。

GitHub Actions: [ビルド済みJAR](https://github.com/Sanowo0390/AutoLuma/actions/workflows/build.yml)（成功した実行のArtifactsから入手）。

## ライセンス

MIT — [LICENSE](LICENSE)
