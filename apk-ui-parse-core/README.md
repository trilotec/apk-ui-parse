# apk-ui-parse-core

Pure Java core module for `APK UI Parse`.

## Responsibilities

- defines snapshot models
- defines dump result and option models
- exports snapshots as JSON
- contains utility code that does not require the Android SDK
- hosts unit tests for the stable data/export layer

## Recommended Distribution

- `jar`

## Package Namespace

```text
com.apkparse.core
```

## Notes

- this module is intended to stay lightweight and testable
- it can be published independently from the Android integration module
- it is the best entry point when only the models and JSON export contract are needed
