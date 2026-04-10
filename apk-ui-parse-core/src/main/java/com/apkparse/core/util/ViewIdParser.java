package com.apkparse.core.util;

public final class ViewIdParser {
    private ViewIdParser() {
    }

    public static String extractViewIdName(String viewId) {
        if (viewId == null || viewId.length() == 0) {
            return null;
        }

        int slashIndex = viewId.lastIndexOf('/');
        if (slashIndex >= 0 && slashIndex < viewId.length() - 1) {
            return viewId.substring(slashIndex + 1);
        }

        int colonIndex = viewId.lastIndexOf(':');
        if (colonIndex >= 0 && colonIndex < viewId.length() - 1) {
            return viewId.substring(colonIndex + 1);
        }

        return viewId;
    }
}
