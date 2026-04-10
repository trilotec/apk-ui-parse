# APK UI Parse

`APK UI Parse` is an Android UI inspection library that captures the current accessibility tree of the foreground app and exports it as JSON.

This repository is designed as a multi-module Android project for open source distribution:

- `apk-ui-parse-core`: pure Java models and JSON export
- `apk-ui-parse-android`: Android accessibility integration
- `apk-ui-parse-sample`: demo app for manual and device testing

## What It Does

- Captures the top window from a running Android app through `AccessibilityService`
- Walks the accessibility node tree and exports a structured JSON snapshot
- Exposes node metadata such as package, activity, class, text, bounds, state flags, depth, and child structure
- Includes a floating inspector demo for cross-app inspection on a real device

## Important Scope

This project does not read the target app's real `View` objects directly.

It inspects the Android accessibility tree, so the output depends on what the target app exposes through `AccessibilityNodeInfo`. Some values may be missing or normalized by the Android accessibility layer.

## Repository Layout

### `apk-ui-parse-core`

Pure Java module that contains:

- snapshot models
- dump result models
- JSON export logic
- utility classes
- unit tests

Primary artifact:

- `jar`

### `apk-ui-parse-android`

Android library module that contains:

- `AccessibilityService`
- active window capture
- node tree walking
- field mapping from `AccessibilityNodeInfo`
- Android-facing dump facade

Primary artifact:

- `aar`

Secondary artifact:

- classes-only `jar`

`aar` is the recommended release format for this module because Android library metadata and manifest integration are not preserved in a plain `jar`.

### `apk-ui-parse-sample`

Sample app used for:

- accessibility permission guidance
- overlay permission guidance
- JSON dump testing
- floating inspector testing
- real-device validation

This module is not the main product.

## Key Features

- Top-window JSON dump
- Accessibility-based cross-app inspection
- Floating overlay inspector in the sample app
- Save and share JSON from the sample app
- GitHub Actions build and release workflows

## Output Model

The snapshot includes fields such as:

- package name
- activity name
- class name
- view id resource name
- content and raw text
- accessibility text, hint, tooltip, pane title
- width and height
- screen bounds and parent-relative bounds
- parent and child node references
- depth and drawing order
- visibility and interaction flags

Exact field coverage depends on Android API level and what the foreground app exposes through accessibility.

## Requirements

- JDK 17
- Android SDK
- Gradle wrapper included in this repository
- A real Android device is strongly recommended for validation

Optional local SDK file:

```properties
sdk.dir=/path/to/Android/Sdk
```

## Build

Build the main artifacts:

```powershell
./gradlew :apk-ui-parse-core:jar
./gradlew :apk-ui-parse-android:assembleRelease
./gradlew :apk-ui-parse-android:exportReleaseJar
./gradlew :apk-ui-parse-sample:assembleDebug
```

Run unit tests:

```powershell
./gradlew :apk-ui-parse-core:test
```

Publish to the local Maven repository:

```powershell
./gradlew :apk-ui-parse-core:publishReleasePublicationToMavenLocal
./gradlew :apk-ui-parse-android:publishReleasePublicationToMavenLocal
```

Publish to the local build repo under `build/maven-repo`:

```powershell
./gradlew publishReleasePublicationToLocalBuildRepoRepository
```

## Release Assets

The GitHub release workflow uploads:

- `apk-ui-parse-core.jar`
- `apk-ui-parse-core-sources.jar`
- `apk-ui-parse-core-javadoc.jar`
- `apk-ui-parse-android-release.aar`
- `apk-ui-parse-android-classes.jar`
- `apk-ui-parse-sample-debug.apk`

Workflow files:

- [build.yml](.github/workflows/build.yml)
- [release.yml](.github/workflows/release.yml)

Push a tag to trigger a release:

```powershell
git tag v0.1.0
git push origin v0.1.0
```

## Integration Notes

For actual Android app integration:

- prefer `apk-ui-parse-android` as an `aar` or source module
- use `apk-ui-parse-core` as a `jar` when you only need the models/export layer
- make sure the host app declares the accessibility service and required XML metadata when not consuming the Android module as an `aar`

Current Gradle publication coordinates:

```text
com.github.apk-ui-parse:apk-ui-parse-core:0.1.0-SNAPSHOT
com.github.apk-ui-parse:apk-ui-parse-android:0.1.0-SNAPSHOT
```

## Sample App

The sample app supports:

- starting a floating overlay tool
- entering inspector mode
- selecting nodes from the current foreground UI
- saving and sharing JSON dumps

The floating inspector was validated on a real device during development. The exact accessibility output still depends on target app behavior and device-specific system state.

## Limitations

- Cross-app inspection requires the user to enable the accessibility service
- Overlay-based tooling requires overlay permission
- Some apps intentionally hide or degrade accessibility data
- Integer view IDs are not reliable across third-party apps; resource name strings are the practical stable identifier
- Activity names come from accessibility window events and may lag briefly during transitions

## Private Design Docs

The local `docs/` directory is intentionally excluded from the public repository and is not part of the open-source surface.

## License

Apache-2.0. See [LICENSE](LICENSE).
