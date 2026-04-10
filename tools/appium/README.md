# Appium Tests

This directory contains minimal real-device tests for the sample app.

Current scope:

- install and launch `apk-ui-parse-sample`
- verify main UI labels exist
- tap the dump button to confirm the screen is interactive
- start the floating tool and verify cross-app inspector entry on a foreground app

Run flow:

```powershell
appium
python tools/appium/smoke_test.py
python tools/appium/cross_app_inspect_test.py
```

Preconditions for `cross_app_inspect_test.py`:

- `APK UI Parse Service` is already enabled in Android accessibility settings
- overlay permission is already granted to `com.apkparse.sample`

The cross-app script validates that tapping the floating ball captures a foreground package other than `com.apkparse.sample`.
