package com.apkparse.android.facade;

import android.accessibilityservice.AccessibilityService;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityNodeInfo;

import com.apkparse.android.capture.ActiveWindowProvider;
import com.apkparse.android.capture.ForegroundWindowSnapshot;
import com.apkparse.android.capture.ForegroundWindowTracker;
import com.apkparse.android.capture.NodeTreeWalker;
import com.apkparse.android.mapper.NodeFieldMapper;
import com.apkparse.core.export.JsonExporter;
import com.apkparse.core.model.DumpErrorCode;
import com.apkparse.core.model.DumpOptions;
import com.apkparse.core.model.DumpResult;
import com.apkparse.core.model.SnapshotMeta;
import com.apkparse.core.model.UiNodeSnapshot;
import com.apkparse.core.model.UiWindowSnapshot;

public class DefaultUiDumpManager implements UiDumpManager {
    private final AccessibilityService accessibilityService;
    private final ForegroundWindowTracker foregroundWindowTracker;
    private final NodeFieldMapper nodeFieldMapper;

    public DefaultUiDumpManager(
            AccessibilityService accessibilityService,
            ForegroundWindowTracker foregroundWindowTracker,
            NodeFieldMapper nodeFieldMapper
    ) {
        this.accessibilityService = accessibilityService;
        this.foregroundWindowTracker = foregroundWindowTracker;
        this.nodeFieldMapper = nodeFieldMapper;
    }

    @Override
    public DumpResult dumpTopWindow() {
        return dumpTopWindow(DumpOptions.defaults());
    }

    @Override
    public DumpResult dumpTopWindow(boolean includeInvisibleNodes) {
        DumpOptions options = new DumpOptions.Builder()
                .includeInvisibleNodes(includeInvisibleNodes)
                .build();
        return dumpTopWindow(options);
    }

    @Override
    public DumpResult dumpTopWindow(DumpOptions options) {
        if (accessibilityService == null) {
            return DumpResult.failure(DumpErrorCode.SERVICE_NOT_CONNECTED, "AccessibilityService is not connected.");
        }

        DumpOptions safeOptions = options == null ? DumpOptions.defaults() : options;
        AccessibilityNodeInfo root = ActiveWindowProvider.getRootInActiveWindow(accessibilityService);
        if (root == null) {
            return DumpResult.failure(DumpErrorCode.ROOT_NODE_NULL, "Active window root node is null.");
        }

        try {
            ForegroundWindowSnapshot foregroundWindowSnapshot = foregroundWindowTracker.snapshot();
            NodeTreeWalker nodeTreeWalker = new NodeTreeWalker(nodeFieldMapper, safeOptions, foregroundWindowSnapshot);
            UiNodeSnapshot rootSnapshot = nodeTreeWalker.walk(root);
            if (rootSnapshot == null) {
                return DumpResult.failure(DumpErrorCode.NO_ACTIVE_WINDOW, "No visible node matched current dump options.");
            }

            UiWindowSnapshot windowSnapshot = new UiWindowSnapshot();
            windowSnapshot.setMeta(buildMeta(root, foregroundWindowSnapshot));
            windowSnapshot.setRoot(rootSnapshot);

            String json = JsonExporter.toJson(windowSnapshot, safeOptions.isPrettyJson());
            return DumpResult.success(windowSnapshot, json);
        } catch (RuntimeException exception) {
            return DumpResult.failure(DumpErrorCode.CAPTURE_FAILED, exception.getMessage());
        } finally {
            recycleIfNeeded(root);
        }
    }

    private SnapshotMeta buildMeta(AccessibilityNodeInfo root, ForegroundWindowSnapshot foregroundWindowSnapshot) {
        DisplayMetrics displayMetrics = accessibilityService.getResources().getDisplayMetrics();

        SnapshotMeta meta = new SnapshotMeta();
        meta.setSchemaVersion("1.0.0");
        meta.setCaptureTime(System.currentTimeMillis());
        meta.setPackageName(root.getPackageName() == null ? foregroundWindowSnapshot.getPackageName() : root.getPackageName().toString());
        meta.setActivityName(foregroundWindowSnapshot.getActivityName());
        meta.setWindowId(root.getWindowId());
        meta.setScreenWidth(displayMetrics.widthPixels);
        meta.setScreenHeight(displayMetrics.heightPixels);
        meta.setSource("accessibility");
        return meta;
    }

    private static void recycleIfNeeded(AccessibilityNodeInfo node) {
        if (node == null) {
            return;
        }
        if (android.os.Build.VERSION.SDK_INT < 33) {
            node.recycle();
        }
    }
}
