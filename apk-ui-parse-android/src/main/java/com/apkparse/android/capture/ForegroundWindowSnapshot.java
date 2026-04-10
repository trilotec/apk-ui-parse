package com.apkparse.android.capture;

public class ForegroundWindowSnapshot {
    private final String packageName;
    private final String activityName;
    private final int windowId;
    private final long eventTime;

    public ForegroundWindowSnapshot(String packageName, String activityName, int windowId, long eventTime) {
        this.packageName = packageName;
        this.activityName = activityName;
        this.windowId = windowId;
        this.eventTime = eventTime;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getActivityName() {
        return activityName;
    }

    public int getWindowId() {
        return windowId;
    }

    public long getEventTime() {
        return eventTime;
    }
}
