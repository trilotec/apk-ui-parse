package com.apkparse.android.service;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.apkparse.android.capture.ForegroundWindowTracker;

public class UiParseAccessibilityService extends AccessibilityService {
    private static final String TAG = "UiParseA11yService";
    private static volatile UiParseAccessibilityService sharedInstance;

    private final ForegroundWindowTracker foregroundWindowTracker = new ForegroundWindowTracker();

    public static UiParseAccessibilityService getSharedInstance() {
        return sharedInstance;
    }

    public ForegroundWindowTracker getForegroundWindowTracker() {
        return foregroundWindowTracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedInstance = this;
        Log.i(TAG, "onCreate");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        sharedInstance = this;
        Log.i(TAG, "onServiceConnected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (sharedInstance == null) {
            sharedInstance = this;
        }
        foregroundWindowTracker.onAccessibilityEvent(event);
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public boolean onUnbind(android.content.Intent intent) {
        Log.i(TAG, "onUnbind");
        sharedInstance = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        sharedInstance = null;
        super.onDestroy();
    }
}
