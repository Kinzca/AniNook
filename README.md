# AniNook

让喜欢的角色走进生活。

Android 原生二维桌宠项目。当前交付 **0.1.0-preview 技术验证工程**，用于继续开发和验证 Android 悬浮行为，尚不是可上架的完整产品。

## 现在能做什么

- Kotlin / Jetpack Compose 首页与原创几何测试角色；点击反馈、待机眨眼。
- 用户主动开启的前台悬浮服务；拖动、调整大小、停止陪伴、通知停止入口。
- 保存位置和大小；锁屏隐藏并暂停测试角色绘制，解锁后恢复的生命周期实现。
- 通过系统文件选择器保存一个 `.inp` / `.inx` 本地副本；64 MiB 上限，替换失败保留旧副本。
- Windows / macOS Gradle Wrapper、单元测试、真实 Android 模拟器悬浮集成测试及 GitHub Actions。

**Inochi2D、JNI 和 OpenGL ES 模型渲染尚未接入。** 页面上的测试角色使用 Android Canvas，不能证明 Inochi2D 兼容、图形性能或模型导入闭环。选中的模型只保存原始字节，不解析、不校验模型内容，也不会替换测试角色。后续继续按既有 Inochi2D 验证路线推进。

Linux 和 **macOS ARM64** 的编译、JVM 测试、Lint 已通过；Android 15 模拟器的应用外悬浮、拖动、停止测试已通过。实际提交与报告见 [验证记录](docs/validation/2026-10-19-preview.md)。尚未进行手机真机测试，也未验证 Inochi2D。

## 在另一台 Mac 接手

```bash
git clone https://github.com/Kinzca/AniNook.git
cd AniNook
```

用 Android Studio 打开仓库根目录。安装 Android SDK Platform 36、Build Tools 36.0.0 和 Platform Tools，Gradle JDK 选择 **JDK 21**，等待同步后运行 `app`。无需安装 Godot、Unity、NDK、D 编译器或 MCP 插件。

命令行检查：

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Windows 对应命令：` .\gradlew.bat testDebugUnitTest lintDebug assembleDebug `。

APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。也可在 [GitHub Actions](https://github.com/Kinzca/AniNook/actions) 的成功运行中下载 `validation-ubuntu-latest`，其中包含 APK 和检查报告。debug APK 只用于开发验证。

完整环境配置、模拟器操作与后续任务见 **[Mac 开发接手指南](docs/development/mac-handoff.md)**。

## 固定构建基线

| 项目 | 当前版本 |
| --- | --- |
| Android Gradle Plugin | 9.0.1，内置 Kotlin 2.2.10 |
| Gradle Wrapper | 9.1.0，分发包 SHA-256 已固定 |
| Compose Compiler | 2.2.10 |
| Compose BOM | 2025.08.01 |
| JDK | 建议 21；生成 Java/Kotlin 17 字节码 |
| compileSdk / targetSdk | 36 / 36 |
| Build Tools | 36.0.0 |
| minSdk | 26，仅为验证工程基线，正式支持范围待实测 |

## 文档导航

1. [项目目标与首版范围](docs/project-goals.md)
2. [当前技术路线与决策状态](docs/technical-roadmap.md)
3. [开发前提与验证计划](docs/readiness-and-validation.md)
4. [验证版实现与边界](docs/development/preview-architecture.md)
5. [Inochi2D Android 深度调研](docs/research/inochi2d-android-2026-10-18.md)
6. [桌宠与角色分享设计背景](docs/design/anime-desktop-pet.md)

早期 Godot、Live2D、名称查询资料保留为历史研究；当前主线是 Android 原生。AniNook 自有代码的对外许可尚未决定；仓库公开不自动授予开源许可。构建工具和依赖的许可见 [第三方说明](THIRD_PARTY_NOTICES.md)。
