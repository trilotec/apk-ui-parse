# APK UI Parse

开源定位已经明确为：这是一个给 Android 宿主工程接入的 UI 解析库，不是一个独立交付的 APK。

仓库目标：

- 主体交付物是可接入 Android 的 `jar` 或源码模块
- 解析当前前台 APK 的顶层可访问 UI 树并输出 JSON
- 基于 Java 开发
- 提供一个 `sample` Demo App 仅用于真机测试、联调和示例接入

当前推荐的开源仓库形态：

- `apk-ui-parse-core`
  - 纯 Java 核心模块
  - 负责数据模型、字段规范化、JSON 导出
  - 可直接发布为 `jar`
- `apk-ui-parse-android`
  - Android 接入模块
  - 负责无障碍采集、节点遍历、快照导出
  - 在 GitHub 仓库中以 Android library module 维护
  - 可按需要产出 `jar`，但宿主仍需手工补 `AndroidManifest` 和无障碍配置
- `apk-ui-parse-sample`
  - 示例 App
  - 只用于测试 SDK，不作为主要产品

推荐接入策略：

- `apk-ui-parse-core`：发布 `jar`
- `apk-ui-parse-android`：发布 `aar`
- `apk-ui-parse-sample`：只发布调试用 `apk`

关键约束：

- 解析其他正在运行的 APK，Android 上必须依赖 `AccessibilityService`
- 普通 `jar` 不能携带 `AndroidManifest.xml`、`xml/accessibilityservice` 配置和 Android 资源
- 因此真正适合 GitHub 开源维护的主形态是“多模块源码仓库”，而不是“只有一个 jar 文件”
- 如果后续要让接入更省事，可以额外发布 `aar`，但当前设计仍以 `jar / module` 为主

建议下一步：

- 先搭好多模块工程骨架
- 先实现 `apk-ui-parse-android` 的最小可用导出链路
- 再整理 GitHub 开源所需的 `LICENSE`、`.gitignore`、发布说明和接入示例

当前工程状态：

- 已完成 Gradle 多模块工程骨架
- 已完成 `apk-ui-parse-core` 的基础模型与 JSON 导出
- 已完成 `apk-ui-parse-android` 的最小无障碍采集链路
- 已完成 `apk-ui-parse-sample` 的演示入口、保存 JSON、分享 JSON
- 已生成 `gradlew` / `gradlew.bat`
- 已补充 `core` 基础单元测试

本地构建前提：

- JDK 17
- Android SDK
- 建议通过 `local.properties` 或环境变量配置 SDK 路径

示例 `local.properties`：

```properties
sdk.dir=D:\\AndroidSdk
```

常用命令：

```powershell
./gradlew :apk-ui-parse-core:jar
./gradlew :apk-ui-parse-android:exportReleaseJar
./gradlew :apk-ui-parse-sample:assembleDebug
./gradlew publishToMavenLocal
./gradlew publishReleasePublicationToLocalBuildRepoRepository
```

当前已验证产物：

- `apk-ui-parse-core/build/libs/apk-ui-parse-core.jar`
- `apk-ui-parse-android/build/libs/apk-ui-parse-android-classes.jar`
- `apk-ui-parse-android/build/outputs/aar/apk-ui-parse-android-release.aar`
- `apk-ui-parse-sample/build/outputs/apk/debug/apk-ui-parse-sample-debug.apk`

示例输出：

- repository release artifact or sample runtime output

GitHub Actions：

- `.github/workflows/build.yml`
- `.github/workflows/release.yml`

发布说明：

- `core` 和 `android` 都已接入 `maven-publish`
- 可发布到 `mavenLocal()`
- 也可发布到本地目录仓库 `build/maven-repo`

接入示例：

```gradle
dependencies {
    implementation "com.github.apk-ui-parse:apk-ui-parse-core:0.1.0-SNAPSHOT"
    implementation "com.github.apk-ui-parse:apk-ui-parse-android:0.1.0-SNAPSHOT"
}
```

开源协作文件：

- `CHANGELOG.md`
- `CONTRIBUTING.md`
- `CODE_OF_CONDUCT.md`
- `SECURITY.md`
- `.github/ISSUE_TEMPLATE/`
- `.github/PULL_REQUEST_TEMPLATE.md`

私有资料说明：

- `docs/` 目录用于本地设计与内部文档，不纳入公开仓库
