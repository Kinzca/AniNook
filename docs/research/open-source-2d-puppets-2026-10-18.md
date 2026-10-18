# 免费开源二维桌宠方案调研

> 后续更新：用户已优先选择 Android 原生开发。最新源码审查与建议见[Inochi2D Android 深度评估](inochi2d-android-2026-10-18.md)。本文以 Godot 为前提的路线及 1–2 日探索预算保留为历史，不代表当前完整移植预算。

日期：2026-10-18。目标：Godot、Android、立绘式二维角色、安装后导入角色、分享和自建资源源。方法：核对官方文档、许可和仓库配置；本轮未安装、编译或真机测试。

## 结论

Inochi2D 是最贴近 Live2D 式参数化立绘的完整开源候选，值得做小范围技术验证；当前证据不足以把它认定为开箱即用的 Godot Android 方案。Godot 自带二维骨骼与网格变形是保底路线，但角色制作模板、导出器、包格式和导入器需要自己建设。两者都不承诺直接读取现成 Live2D moc3 模型。

不要以“运行库开源”代替对创作工具、运行时、模型许可和移动端支持的分别核实。也不要把编辑器导入成功视为 APK 安装后用户导入成功。

## 候选对比

| 方案 | 许可与创作工具 | Godot / Android 证据 | 玩家角色包 | 本项目判断 |
| --- | --- | --- | --- | --- |
| Inochi2D + Inochi Creator | 核心与编辑器 BSD-2-Clause | 官方 main 有 Godot GDExtension；所查扩展配置仅列 Windows/Linux，未列 Android | 使用其 INP/INX 模型格式，仍需实现安装后的导入和互动映射 | 视觉目标最接近，移动适配先验证 |
| Godot 原生二维骨骼/网格 | Godot MIT；制作也可在 Godot 完成 | 内置 Skeleton2D、Bone2D、Polygon2D；用户数据读取有官方 API | 自定义 JSON + PNG + 动作/参数数据 | 对外部 SDK 依赖最少，但创作者工具工作量由我们承担 |
| DragonBones + Godot 插件 | 运行库/插件 MIT；不能据此推断完整编辑器链同样开源 | Godot 4.3+ 插件，配置有 Android arm64 等库项 | 骨骼数据、图集和图片；运行时导入还需验证 | 更偏骨骼动画，作为动作表现备选 |
| Rive | 运行库 MIT；不能视为完整开源创作链 | 官方有 Android 运行库，所查官方列表未列 Godot | .riv 可运行时加载，Godot 集成另做 | 不作为首选 |

### Inochi2D

它通过参数驱动分层图片的网格变形，适合立绘表现；官方 Inochi Creator 用来绑定和制作模型。BSD-2-Clause 允许按许可保留声明后使用和分发软件，未设 Live2D 式可扩展应用审批条款；角色作品的授权由作者决定。

[官网与示例](https://inochi2d.com/)、[SDK 许可](https://github.com/Inochi2D/inochi2d/blob/main/LICENSE)、[编辑器](https://github.com/Inochi2D/inochi-creator)、[官方许可说明](https://github.com/Inochi2D/inochi2d/wiki/Legal-Info)。

技术证据：main README 描述 Godot 官方绑定及 INP/INX 加载；模块用 D/LDC 与 nugodot 绑定构建。本轮看到的 Release 页 Latest 标为 v0.8.7，不能把 main 上的新接入能力直接归入稳定发布。实际使用须固定匹配的编辑器、SDK 和模型格式版本。

关键限制：当前 gdextension 文件仅配置 Windows/Linux 库，没有 Android 项。这不能证明无法移植，但足以说明还不能承诺现成 APK 路径；还需处理 Android ABI、工具链、渲染与导出后外部文件加载。官方 CPU 列出 Aarch64，不等于 Android 成品支持。

[SDK README](https://github.com/Inochi2D/inochi2d)、[Godot 构建配置](https://github.com/Inochi2D/inochi2d/blob/main/modules/godot/dub.sdl)、[Godot 平台库配置](https://github.com/Inochi2D/inochi2d/blob/main/modules/godot/res/inochi2d.gdextension)、[发布页](https://github.com/Inochi2D/inochi2d/releases)。

Live2D 与 Inochi2D 格式不同；不能直接将 moc3 改后缀或假设无损转换。已有分层原画在获得相应授权后可重新绑定，动作和参数一般需要重新配置。[官方文档中的兼容说明](https://docs.inochi2d.com/_/downloads/en/latest/pdf/)。

### Godot 原生

建议使用分层图片、骨骼/网格和有限的参数插值制作呼吸、眨眼、表情、头部倾斜及衣发运动。精细转脸、多轴形变、遮罩和物理摆动需要额外制作和工具支持，不能宣称与 Cubism 全部能力等价。

创作者初期使用我们提供的 Godot 模板与导出器，导出纯数据角色包；玩家只需导入文件，不需要安装 Godot。运行时读取 PNG/JSON 后，由内置代码构造许可的节点、网格和动画。不要直接执行陌生场景脚本或挂载不可信 PCK。这是待实现方案，不是 Godot 现成的桌宠格式。

[二维骨骼官方教程](https://docs.godotengine.org/en/stable/tutorials/animation/2d_skeletons.html)、[运行时文件读取](https://docs.godotengine.org/en/stable/tutorials/io/runtime_file_loading_and_saving.html)、[Godot 许可](https://godotengine.org/license/)。

### DragonBones 与 Rive

DragonBones 的 Godot 插件维护者注明支持 Godot 4.3+、DragonBones Pro 5.6，扩展清单列出 Android 库。该配置并不是本项目实测证据；编辑器获取、许可、外部模型加载、形变效果仍需验证。[插件](https://github.com/Daylily-Zeleen/Godot-DragonBones)、[平台配置](https://raw.githubusercontent.com/Daylily-Zeleen/Godot-DragonBones/master/demo/addons/godot_dragon_bones.daylily-zeleen/godot_dragon_bones.gdextension)。

Rive 的 C++ 运行库是 MIT。当前定价页 Free 计划允许带 Rive 启动画面的 .riv 导出；网上“免费版完全不能导出”的旧说法不能沿用。尽管如此，运行库开源并不满足本项目对整个创作链开放的期待，且官方平台列表未列 Godot，因此不优先考虑。[运行库许可](https://github.com/rive-app/rive-runtime/blob/main/LICENSE)、[当前计划](https://rive.app/pricing)、[官方产品平台](https://rive.app/editor)。

## 导入与社区设计

导入/分享是独立于渲染器的产品能力，任何候选都不会自动提供完整桌宠社区。

建议外层采用版本化角色包：manifest（作者、名称、包版本、渲染器类型、兼容版本、许可）、模型数据、贴图、动作/参数映射、封面与可选音频。一次只实现一种渲染后端，但外层不锁死模型类型。

流程：创作者制作并导出 → 玩家本地导入 → 预览和选择 → 手机显示；相同包还可上传到授权共享源。自建源先提供 HTTPS 目录与下载，公共上传服务后做。下载到应用私有目录后可离线使用；在 Android 用系统文件选择器取得用户明确选择的文件。

导入时检查包版本、资源完整性、路径范围、解压与纹理大小、数据数量；仅接收声明式内容。共享源保留作者授权和投诉下架信息。软件开源不能推导为所有模型可公开再分发。

## 下一步验证建议

1. 给 Inochi2D 设置约 1–2 个开发日的探索上限（预算建议，不是成功承诺）：先确认可构建匹配版本，再验证 Android arm64 路径；一旦显示需要较大移植工作，记录并转保底方案。
2. 样本必须覆盖已安装 APK 导入外部角色、驱动眨眼/表情/头部参数、切换和释放模型、重启恢复。自带资源能显示不足以通过。
3. 独立验证透明悬浮、触摸范围、服务与渲染生命周期、锁屏暂停、内存和耗电。更换角色渲染库不会消除这些 Android 工作。
4. 若采用 Godot 原生方案，只先实现一个简化角色和一个创作者模板，验证二维观感是否满足用户；不先开发完整 Live2D 替代编辑器。
5. 角色质量和导入体验通过后再实现资源源协议与小规模分享。

选型尚未锁定；本轮交付是调研与验证顺序，没有实际运行结果或部署。
