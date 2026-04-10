package com.apkparse.android.capture;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityNodeInfo;

public final class ActiveWindowProvider {
    private ActiveWindowProvider() {
    }

    public static AccessibilityNodeInfo getRootInActiveWindow(AccessibilityService service) {
        if (service == null) {
            return null;
        }
        return service.getRootInActiveWindow();
    }
}
