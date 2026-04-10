# Contributing

## Development Setup

Prerequisites:

- JDK 17
- Android SDK with platform `android-35`
- Gradle wrapper

Recommended local setup:

```properties
# local.properties
sdk.dir=D:\\AndroidSdk
```

Common commands:

```powershell
./gradlew :apk-ui-parse-core:test
./gradlew :apk-ui-parse-android:assembleRelease
./gradlew :apk-ui-parse-sample:assembleDebug
./gradlew publishToMavenLocal
```

## Contribution Rules

- Keep changes scoped and reviewable.
- Do not commit generated `build/` output.
- Update docs when public behavior or JSON schema changes.
- Preserve backward compatibility for exported JSON fields where possible.

## Pull Requests

Before opening a pull request:

- Run relevant Gradle builds and tests locally
- Update `CHANGELOG.md` for user-visible changes
- Add or update tests when touching core export behavior

## Issue Quality

Useful bug reports include:

- Android version
- Device/ROM information
- Target app package name
- Whether the screen is View-based, Compose-based, WebView, or custom rendered
- Sample exported JSON or screenshots when possible
