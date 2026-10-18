# Mac 开发接手指南

更新：2026-10-19。目标是在另一台 Mac 上继续开发同一个 Android 项目；当前不构建 macOS 桌面应用。

## 1. 获取工程

```bash
mkdir -p ~/Projects
cd ~/Projects
git clone https://github.com/Kinzca/AniNook.git
cd AniNook
git status
```

已有克隆则先提交或保存本地改动，再 `git pull --ff-only`。提交后才能在另一台电脑取得修改；不要只复制 APK。

## 2. Android Studio 和 SDK

1. 安装支持 AGP 9.0 的 Android Studio（Otter 3 / 2025.2.3 或更新版）。Apple Silicon 选择 Mac ARM 安装包。
2. 用 Open 打开包含 `settings.gradle.kts` 的 AniNook 根目录，允许 Gradle 同步。
3. SDK Manager → SDK Platforms：安装 **Android 16 / API 36**。当前工程并不要求 API 37。
4. SDK Tools：安装 **Android SDK Build-Tools 36.0.0**、Platform-Tools；需要模拟器时安装 Android Emulator。命令行管理 SDK 再安装 Command-line Tools。
5. Settings → Build, Execution, Deployment → Build Tools → Gradle：Gradle JDK 选择 **21**。未安装时用 Download JDK 下载 21。不要把 Windows JDK 或 SDK 路径复制到 Mac。
6. 本机 SDK 路径由 Android Studio 写入 `local.properties`，该文件已被 Git 忽略。常见路径为 `~/Library/Android/sdk`。

第一次同步需要访问 Google Maven、Maven Central 和 Gradle 分发服务。工程没有绑定第三方镜像或私人代理。若下载失败，先检查网络和 Android Studio / Gradle 的本机代理配置，不把代理密码提交进 Git。

工具兼容依据：[AGP 9.0.1 官方说明](https://developer.android.com/build/releases/agp-9-0-0-release-notes)、[Gradle JDK 设置](https://developer.android.com/build/jdks)。

## 3. 构建与运行

Android Studio 选择 `app`，点击 Run。命令行等价检查：

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew testDebugUnitTest lintDebug assembleDebug
```

如 JDK 由 Android Studio 下载且 `java_home` 找不到，使用 Studio 中显示的 JDK 目录设置 JAVA_HOME，或直接从 IDE 执行 Gradle。仓库包含完整 Wrapper；不需要另外全局安装 Gradle。若从 ZIP 下载导致执行位丢失，运行 `chmod +x gradlew`。

调试 APK：`app/build/outputs/apk/debug/app-debug.apk`。CI 也会上传 APK 与报告到 Actions，保留 14 天，不将二进制产物塞进源码仓库。

不同电脑/CI 的默认 debug 签名通常不同。若安装提示签名不一致，可继续用同一台机器的签名构建；卸载旧验证包再安装也可解决，但会删除其本地设置与导入副本，请自行决定。

## 4. 第一次验证

Device Manager 创建 Pixel 模拟器，优先 API 35 或 36。Apple Silicon 使用 **arm64-v8a** 系统镜像；Intel Mac 使用 **x86_64**。当前工程只有 Kotlin/Java，不含限制 CPU 架构的 native 库。

1. 打开 AniNook，确认首页出现“测试占位角色”；点击角色有反馈。
2. 开启“悬浮窗口”权限，返回应用；开启通知权限，方便从通知停止。
3. 点击“开始陪伴”，回到桌面和另一个应用，确认角色仍显示；拖动到四个角落。
4. 回到应用，调整大小；旋转设备后确认角色仍可找回。
5. 锁屏再解锁，检查隐藏、恢复及角色动画。
6. 从通知点击“停止陪伴”，确认悬浮窗口消失；再开启并从应用内停止。
7. 开启后撤销悬浮权限，检查应用和服务的实际表现。
8. 从文件管理器选取有权使用的 `.inp` / `.inx`，确认只提示“已保存，待兼容性验证”，重启应用后记录还在；该版本不会显示导入模型。
9. 导入空文件、错误扩展名和超过 64 MiB 的文件，确认失败时原副本保留。删除副本不会删除用户选取的原文件。

模拟器结果只能证明相应系统环境；厂商后台限制、真实触摸体验、GPU、续航必须另外在手机上验证。

已连接模拟器/手机时可运行：

```bash
./gradlew connectedDebugAndroidTest
```

集成测试会临时通过 ADB appops 授予悬浮权限，测试后恢复 default。它覆盖实际 WindowManager 窗口出现、拖动、停止，不覆盖用户手动授权页面、锁屏功耗或 Inochi2D。

## 5. 代码位置与下一步

| 位置 | 用途 |
| --- | --- |
| `app/src/main/java/io/github/kinzca/aninook/MainActivity.kt` | 系统权限、文件选择、界面入口 |
| 同目录 `ui/HomeScreen.kt` | Compose 首页 |
| `overlay/PetOverlayService.kt` | 前台服务、窗口、拖动、屏幕状态 |
| `preview/DiagnosticPetView.kt` | 可删除的 Canvas 验证角色，无模型引擎 |
| `imports/PendingModelStore.kt` | 单个本地副本、大小限制、失败保留 |
| `app/src/test` / `app/src/androidTest` | JVM 测试 / Android 窗口集成测试 |
| `docs/research/inochi2d-android-2026-10-18.md` | 下一阶段 native 接入的源码研究基线 |

建议接手顺序：

1. 先复现本次构建结果并完成上述手工检查，记录 Mac 型号、JDK、SDK、设备与提交。
2. 准备允许用于验证的 Inochi2D 模型和明确的 Creator 版本；不要把私人或无分发许可模型放进公开仓库。
3. 单独建立 **Android arm64 D 核心 + C ABI / JNI 最小加载样本**，锁定 LDC/DUB/NDK 与依赖。验证基线提交 `4975d247f9b946a74d18e0ba9b3e9475eb636efb` 只是研究依据，不是已成功移植版本。
4. 取得加载证据后实现 OpenGL ES 渲染、遮罩/合成与三组模型对照；通过后再把测试角色接入点替换为真正角色会话。

当前没有伪造的 native 空实现，也没有以文件扩展名冒充格式兼容校验。完整桌宠产品、共享社区与 TapTap 上架仍需后续开发和验证。

MCP 插件是可选开发便利工具，不影响构建。Windows 本机的 Codex/MCP 配置未提交，Mac 需要时单独配置。
