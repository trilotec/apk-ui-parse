package com.apkparse.android.capture;

import android.view.accessibility.AccessibilityEvent;

public class ForegroundWindowTracker {
    private String packageName;
    private String activityName;
    private int windowId = -1;
    private long eventTime;

    public synchronized void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) {
            return;
        }

        CharSequence eventPackage = event.getPackageName();
        if (eventPackage != null) {
            packageName = eventPackage.toString();
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.getClassName() != null) {
            activityName = event.getClassName().toString();
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            windowId = event.getWindowId();
        }
        eventTime = event.getEventTime();
    }

    public synchronized ForegroundWindowSnapshot snapshot() {
        return new ForegroundWindowSnapshot(packageName, activityName, windowId, eventTime);
    }
}
