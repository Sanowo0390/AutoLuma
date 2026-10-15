# AutoLuma

[![Build](https://github.com/Sanowo0390/AutoLuma/actions/workflows/build.yml/badge.svg)](https://github.com/Sanowo0390/AutoLuma/actions/workflows/build.yml)

**Automatic brightness for Minecraft Java Edition 26.3 (Fabric).**

暗い場所に入ると徐々に画面を明るくし、明るい場所に戻ると元の明るさに復帰するFabricクライアントMod。

> **Status:** Experimental v1.1.1. Build tests and GLSL validation run on GitHub Actions. Visual results, shader-pack compatibility, and game startup have not been manually verified. This is **not a stable release**.

## Crash fix in v1.1.1 / クラッシュ修正

Minecraft 26.3 may call `GameRenderer.nightVisionScale()` even when the
player has no real Night Vision potion effect, because AutoLuma enables the
client-side visual night vision branch. Earlier builds (up through v1.1.0)
called vanilla's implementation unconditionally, causing a
`NullPointerException: nightVision is null` in dark areas with no potion.
Version 1.1.1 checks for a real effect instance before delegating to
vanilla and adds regression tests. A successful build does not by itself
verify the complete render pipeline in-game.

v1.1.0以前で暗視ポーション効果がない場合に発生したNullPointerExceptionを修正。
実際のポーション効果がない場合はバニラの暗視強度計算を呼ばず、
AutoLuma自身の補正値を返します。既存のバニラ暗視効果も保持します。

## Features / 機能

- **Continuous exposure:** darker areas receive more correction; smoothly fades out in daylight.
- **Screen post-processing:** lifts dark pixels *after* world rendering. Unlike lightmap gamma, it can lift even near-black output. Added in **Minecraft 26.3**.
- **Auto mode:** selects client-side Night Vision in vanilla rendering, and a screen post-effect when Iris is actively rendering a shader pack. Manual rendering modes remain available.
- **Mod Menu configuration:** 0–100% **correction strength**, light threshold 0–14, transition time 1–60 ticks, enable/disable, rendering mode, and temporary force-apply diagnostic mode.
- **Immediate preview:** changing a slider refreshes the correction without waiting for the world to resume from the pause menu.
- **Diagnostics:** effective/block/sky/darkening light values, strength %, active post-effect level, Iris shader status and engine callback counters.
- **Client-side:** no server install, no `options.txt` mutation, no fake potion effects, no changing server lighting.
- **Japanese and English** included.

## How it works / 補正方式

| Mode | What it does | When to try |
| --- | --- | --- |
| **Auto (default)** | Night Vision in vanilla; post-effect when Iris shaders are active | Recommended first; changes renderer according to the active shader pack |
| Post-effect | Full-screen contrast-preserving dark lift, applied between world rendering and HUD | Primary rendering backend for 26.3 |
| Night vision | Client-only vanilla night-vision lightmap branch (no potion status) | Fallback for shader packs that support vanilla night vision |
| Gamma | Adds transient brightness to the vanilla lightmap | Low overhead, suitable for vanilla lighting but may have no effect with shaders |

### Shader compatibility

ScalableLux modifies world lighting calculations; AutoLuma operates on the client render pipeline. They are not known to directly conflict, but compatibility is not guaranteed.

Iris may replace custom core shaders and shader packs are free to implement their own lighting. A Minecraft 26.3 post effect is executed after world rendering by the vanilla GameRenderer pipeline, so it should be more robust than gamma in many situations, **but it is not guaranteed across all Iris packs or mod combinations**. The Night Vision and Gamma alternatives remain available.

In particular, **Auto does not claim universal compatibility or patch Iris shader packs directly**. You can disable the active Iris shader pack to compare results. If all modes have no effect, attach logs and the Mod Menu diagnostic values to an Issue.

### Important: percentage semantics

**Strength 0–100% is an artistic correction parameter, not a screen-brightness or luminance multiplier.** At 100%, the post-effect raises shadow detail and adds a small black-level lift; increasing this is *not* equivalent to multiplying physical brightness. Darker environments receive more correction automatically.

The first launch of v1.1.0 migrates older settings: v1.0.x `boostPercent=500` becomes `strengthPercent=33`, preserving the proportional slider position, and the old `useNightVision=true` preference maps to Night Vision mode. Existing `darkThreshold` and `transitionTicks` are retained. Save the configuration in Mod Menu to write the new format.

## Requirements

- Minecraft **Java Edition 26.3**
- **Fabric Loader 0.19.5+** and Fabric API for 26.3
- **Mod Menu 21.0.0** for settings (optional for gameplay)
- Java **25** for building

## Install

1. Install Fabric Loader + Fabric API.
2. Download the JAR from a successful [GitHub Actions build](https://github.com/Sanowo0390/AutoLuma/actions/workflows/build.yml) (Artifacts section). **Do not download an untested build as if it were a stable release.**
3. Place the JAR in your client's `mods` folder.
4. Open Mod Menu → AutoLuma → Configure.
5. Start with Auto mode, threshold 11 and 70% correction strength. To test the full-screen dark lift without Iris, explicitly select Post-effect mode.

Settings: `config/autoluma.json`. Local settings and secrets are not committed.

## Verification checklist / 動作確認

1. In daylight, the intensity readout should fall to 0%, with post level 0/12.
2. In a pitch-black cave, intensity should rise smoothly. In Auto mode with no Iris shader pack active, Night Vision is used, so post level remains 0/12. For post-processing tests, select Post-effect mode explicitly.
3. Enable **Always apply (diagnostic)** at 100% strength. Closing the menu should visibly brighten the whole world, even dark pixels.
4. Change the strength from 0% to 100% while paused; it should preview the target intensity immediately.
5. Read **Post queued**: if the counter increases, the client has requested AutoLuma's post effect. This confirms an attempt to schedule it, **not** that the GPU successfully rendered it.
6. If nothing changes visually, try manually selecting Night Vision, then Gamma, and compare with Iris shaders disabled.
7. When testing with ScalableLux, repeat once with only Fabric API, Mod Menu and AutoLuma active to isolate another lighting mod.
8. Verify server-side post effects continue to work (AutoLuma appends to an independent copy of the requested effect list).

### Runtime instrumentation

**Gamma/NV hook counts** are incremented when the relevant render method runs. The **Post queued** counter measures requests for post-processing, not successful GPU execution. The current test suite validates source calculations, the JAR build and GLSL syntax, but does *not* perform screenshot comparison or start a graphics client. Please do not report those parts as verified.

## Build

Requires JDK 25 and Gradle 9.7.1:

```bash
gradle clean build
```

On Windows, run `./build.ps1` in PowerShell. This fetches official Gradle and validates its SHA-256. The result is `build/libs/autoluma-1.1.1.jar`.

The GitHub Actions workflow additionally validates the fragment shader with `glslangValidator` and executes unit tests, including checks of the twelve screen-effect definitions. No release is automatically published.

## Notes

Post-processing intentionally runs after world rendering and before the normal HUD so HUD elements are not brightened unnecessarily. Multiple screen post effects will run in sequence; if a server or resource pack already applies post-processing, the final look will depend on their order.

Minecraft, Fabric, Iris and Mod Menu belong to their respective rights holders. AutoLuma is an unofficial independent project.

## License

MIT © 2026 Sanowo0390 and contributors — see [LICENSE](LICENSE).
