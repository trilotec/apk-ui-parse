package com.apkparse.core.model;

public class DumpResult {
    private final boolean success;
    private final DumpErrorCode errorCode;
    private final String errorMessage;
    private final String json;
    private final UiWindowSnapshot snapshot;

    private DumpResult(boolean success, DumpErrorCode errorCode, String errorMessage, String json, UiWindowSnapshot snapshot) {
        this.success = success;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.json = json;
        this.snapshot = snapshot;
    }

    public static DumpResult success(UiWindowSnapshot snapshot, String json) {
        return new DumpResult(true, DumpErrorCode.NONE, null, json, snapshot);
    }

    public static DumpResult failure(DumpErrorCode errorCode, String errorMessage) {
        return new DumpResult(false, errorCode, errorMessage, null, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public DumpErrorCode getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getJson() {
        return json;
    }

    public UiWindowSnapshot getSnapshot() {
        return snapshot;
    }
}
