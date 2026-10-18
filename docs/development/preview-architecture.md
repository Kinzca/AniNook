# 0.1 验证版实现与边界

本次先让 Android 外壳能够独立开发和验证。既有 Inochi2D 候选路线保持不变；没有声称模型核心已移植。

## 实现

- `MainActivity` 管理系统权限、文件选择和 UI。文件复制在 IO 线程执行，不依赖外部存储全盘权限。
- `PetOverlayService` 仅由用户在前台主动启动，使用 `TYPE_APPLICATION_OVERLAY`、`specialUse` 前台服务和持续通知。没有启动广播、后台自拉起、无障碍服务或强制忽略电池优化。
- 单个小尺寸窗口接收点击与拖动，不获取焦点；窗口外触摸留给其他应用。透明像素仍在矩形触摸区域内，不宣称逐像素穿透。
- 保存像素位置与 dp 大小，旋转和调整大小后按当前屏幕重新约束位置。屏幕关闭或锁屏时隐藏、暂停绘制，解锁恢复；具体机型结果需要测试。
- `DiagnosticPetView` 是本项目代码绘制的简单测试角色，共用于首页和悬浮窗口。仅验证窗口和生命周期；当前没有 EGL Surface、JNI、D 核心或模型物理。
- `PendingModelStore` 保留一个用户选择文件的本地副本。内部随机文件名，读取过程限制 64 MiB；新文件和元数据写好后原子替换记录，失败保留旧记录。仅以扩展名筛选候选文件，没有对模型内容作可信性或兼容性背书。

## 数据与权限

应用未声明 INTERNET，无登录、广告、分析统计、上传或共享服务。副本放在应用私有目录，卸载时删除；应用内移除副本不修改源文件。当前不申请持久读取用户源文件的权限。`allowBackup=false` 关闭云端自动备份；Android 12+ 的系统设备间迁移尚未配置独立 dataExtractionRules，也未测试，不能声称可阻止系统迁移应用数据。

悬浮权限由用户手动授权；通知权限可拒绝，但拒绝时通知栏停止入口可能不可见，应用内仍可停止。Android 也可能终止服务，不保证永久后台存活。撤销悬浮权限的进程/窗口处理由实际 Android 版本决定，服务内增加了权限变化监听。

官方依据：[悬浮窗口类型](https://developer.android.com/reference/android/view/WindowManager.LayoutParams#TYPE_APPLICATION_OVERLAY)、[specialUse 前台服务](https://developer.android.com/develop/background-work/services/fgs/service-types#special-use)、[通知运行时权限](https://developer.android.com/develop/ui/views/notifications/notification-permission)。选择这些系统机制不代表任何应用商店已接受其用途。

## 还没有实现

Inochi2D 构建与加载、模型解析、真实二维形变和表情参数、OpenGL ES 渲染、角色包规范、完整角色库与切换、资源服务器、公共分享、商业化与上架材料。为避免制造无效架构，本版没有预先铺设这些功能的空壳接口。

## 构建和测试

固定版本见 README。CI 在 Linux 与 macOS 构建、运行 JVM 测试和 Android Lint，并另启 Android 15 x86_64 模拟器执行真实窗口测试。所有任务的执行状态以 Actions 和验证记录为准。

JVM 用例只覆盖文件副本存储及坐标约束；模拟器用例覆盖应用外窗口、拖动和停止。两者均不能证明 Inochi2D、真机功耗、厂商后台策略或公开发布合规。
