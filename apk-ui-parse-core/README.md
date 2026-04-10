# apk-ui-parse-core

纯 Java 核心模块。

这是主产品的一部分，可独立发布为 `jar`。

用于放置：

- 通用数据模型
- JSON 导出器
- 字段规范化器
- 与 Android 解耦的遍历结果结构

开源仓库角色：

- GitHub 主体模块
- 负责稳定的数据结构和导出协议
- 尽量不依赖 Android SDK，便于测试和发布

建议包前缀：

- `com.apkparse.core`
