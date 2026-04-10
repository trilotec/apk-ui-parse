package com.apkparse.android.facade;

import com.apkparse.core.model.DumpOptions;
import com.apkparse.core.model.DumpResult;

public interface UiDumpManager {
    DumpResult dumpTopWindow();

    DumpResult dumpTopWindow(boolean includeInvisibleNodes);

    DumpResult dumpTopWindow(DumpOptions options);
}
