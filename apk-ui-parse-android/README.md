# apk-ui-parse-android

Android 接入模块。

这是主产品的一部分，建议在 GitHub 仓库中以 Android library module 维护。

用于放置：

- `AccessibilityService`
- 前台窗口跟踪器
- 根节点获取器
- `AccessibilityNodeInfo` 到核心模型的字段映射器

开源仓库角色：

- GitHub 主体模块
- 提供 Android 侧接入能力
- 可构建产出 `jar`，但宿主仍需补充 Manifest 和无障碍配置

建议包前缀：

- `com.apkparse.android`
