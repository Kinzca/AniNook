# 第三方组件说明

- Gradle Wrapper 9.1.0：来自 [Gradle 官方源码 v9.1.0](https://github.com/gradle/gradle/tree/v9.1.0/gradle/wrapper)，Apache License 2.0。启动脚本保留原版权声明，许可全文见 `licenses/Apache-2.0.txt`。
- Android Gradle Plugin、AndroidX / Jetpack Compose：Google / Android Open Source Project，主要采用 Apache License 2.0；通过官方 Maven 仓库下载，不将 SDK 本体复制进仓库。
- Kotlin 标准库和 Compose Compiler 插件：JetBrains / Kotlin，Apache License 2.0。
- JUnit 4.13.2：Eclipse Public License 1.0，仅测试依赖。
- GitHub Actions 的各 action 按其仓库许可运行，不包含在 APK 中。

实际依赖树可用 `./gradlew :app:dependencies` 查询。正式分发前还需按最终 APK 依赖补齐许可审查与展示。

当前没有捆绑 Inochi2D、Live2D、外部角色模型或 Unity 资产。测试角色由本项目几何绘制代码生成，不是正式默认角色。AniNook 自有代码的对外授权尚未决定；本文件不是给整个工程授予 Apache 许可。
