# AutoLuma

Automatically brighten dark places in **Minecraft Java Edition 26.3 (Fabric)**, without permanently changing the game's brightness setting.

暗い場所だけ自動的に明るくする、**Minecraft Java Edition 26.3 対応・Fabric クライアント用Mod**です。

## Features / 主な機能

- Automatically applies extra gamma in dark areas and gradually returns to normal in bright areas. / 暗所で明るくなり、明るい場所に戻ると自然に復帰
- Configure **Gamma boost: 0–1500%** in **Mod Menu**. / Mod Menuから増加率を変更
- Configurable darkness threshold (light level 0–14), transition smoothing (1–60 ticks), and enable/disable switch.
- Two-position light sampling and hysteresis reduce flicker. / 足元・目線の光量を確認し、頻繁な切り替えを抑制
- Client-side only. Does not edit `options.txt`. / サーバー側への導入不要、通常の明るさ設定を保持
- Japanese and English UI. / 日本語・英語対応
- Separate block/sky-light sampling with night-time correction (default threshold: **11**).
- A live readout of nearby light and gamma, plus an **Always apply (test mode)** toggle to distinguish detection issues from rendering/shader issues.

## Checking if AutoLuma works / 動作確認

Open Mod Menu → AutoLuma → Configure. Read the **Light / Block / Sky / Sky darken** diagnostic values and **Applied bonus** (the previous tick's values are shown while the single-player game is paused in menus).

- In full daylight, effective light should be around 15 and the bonus normally drops to zero.
- In a cave or outdoors at night, effective light should fall below the chosen threshold (default 11), causing a smooth gamma boost.
- If **Always apply (test mode)** brightens the screen but auto mode does not, raise the darkness threshold.
- If **Always apply** reports a positive bonus but there is no visual change, test without Iris shaders or other lightmap/fullbright mods. Custom shaders may bypass vanilla lightmap gamma.
- Disable test mode to resume automatic behavior.

Existing `config/autoluma.json` files retain the previous darkness threshold (7 by default in v1.0.0). To use the new default of 11, move the slider or remove the old config file while the game is closed.

> **Note / 注意:** The percentage is an extra **gamma amount**, **not a linear increase in screen luminance**. For example, +500% means adding 5.0 to the lightmap gamma. Shader packs with custom lighting may not respond to this adjustment. Darkness detection uses local game light levels, not image-based auto exposure.
>
> **注意：** 「500%」は画面の輝度が5倍になる意味ではなく、ライトマップのガンマに5.0を加えます。シェーダーによっては効果が弱い／出ない場合があります。

## Requirements / 動作環境

| Component | Version |
| --- | --- |
| Minecraft: Java Edition | 26.3 |
| Java / JDK | 25 (JDK is needed to build) |
| Fabric Loader | 0.19.5 or newer |
| Fabric API | Version compatible with 26.3 |
| Mod Menu | 21.0.0 (optional for gameplay; required for configuration UI) |

## Installation / 導入方法

1. Install Fabric Loader and Fabric API for Minecraft 26.3. / 26.3用Fabric LoaderとFabric APIを導入
2. Place the built AutoLuma JAR into your client's `mods/` folder. / JARをクライアントの`mods`へ
3. Install **Mod Menu 21.0.0** to edit settings. / 設定変更にはMod Menu 21.0.0が必要
4. Open **Mods → AutoLuma → Configure**. / Mod MenuのAutoLumaから設定画面を開く

Configurations are saved to `config/autoluma.json`.

## Building / ビルド

Requires **JDK 25**. On Windows, run `./build.ps1` in PowerShell; it downloads official Gradle 9.7.1 if necessary. Alternatively, with Gradle installed:

```shell
gradle build
```

Result: `build/libs/autoluma-1.0.1.jar`. GitHub Actions also compiles the project and uploads build artifacts for each push and pull request.

**Verification status:** API references have been reviewed for Minecraft 26.3. Runtime behavior and third-party shader compatibility have **not yet been verified in-game**. Do not treat an untested build as a stable release.

## Compatibility / 互換性

This mod only changes the client-side extracted **lightmap gamma**. It does not alter server-side lighting, block light, night vision, or the user's saved vanilla brightness slider. Other mods that patch the same lightmap pipeline can conflict. Please respect multiplayer server rules.

Minecraft、Fabric、Mod Menuはそれぞれの権利者に帰属します。AutoLumaは非公式のModです。

## License / ライセンス

MIT © 2026 Sanowo0390 and contributors. See [LICENSE](LICENSE).
