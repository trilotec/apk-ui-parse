# apk-ui-parse-android

Android integration layer for `APK UI Parse`.

## Responsibilities

- hosts the `AccessibilityService`
- captures the active foreground window
- walks the accessibility node tree
- maps `AccessibilityNodeInfo` into repository snapshot models
- exposes the Android-facing dump API

## Recommended Distribution

Preferred format:

- `aar`

Optional format:

- classes-only `jar`

Use the `aar` when possible. A plain `jar` cannot carry Android manifest entries, service declarations, or XML metadata required by accessibility integration.

## Package Namespace

```text
com.apkparse.android
```

## Main Entry Points

- `com.apkparse.android.facade.UiParse`
- `com.apkparse.android.service.UiParseAccessibilityService`

## Notes

- this module depends on `apk-ui-parse-core`
- cross-app inspection requires accessibility permission on the device
- exported data depends on what the target app exposes through Android accessibility
