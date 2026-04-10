package com.apkparse.android.facade;

import com.apkparse.android.mapper.NodeFieldMapper;
import com.apkparse.android.service.UiParseAccessibilityService;
import com.apkparse.core.model.DumpErrorCode;
import com.apkparse.core.model.DumpOptions;
import com.apkparse.core.model.DumpResult;

public final class UiParse {
    private UiParse() {
    }

    public static boolean isServiceConnected() {
        return UiParseAccessibilityService.getSharedInstance() != null;
    }

    public static DumpResult dumpTopWindow() {
        return dumpTopWindow(DumpOptions.defaults());
    }

    public static DumpResult dumpTopWindow(DumpOptions options) {
        UiParseAccessibilityService service = UiParseAccessibilityService.getSharedInstance();
        if (service == null) {
            return DumpResult.failure(DumpErrorCode.SERVICE_NOT_CONNECTED, "UiParseAccessibilityService is not connected.");
        }

        return new DefaultUiDumpManager(
                service,
                service.getForegroundWindowTracker(),
                new NodeFieldMapper()
        ).dumpTopWindow(options);
    }
}
