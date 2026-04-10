package com.apkparse.sample;

import com.apkparse.core.model.RectSnapshot;
import com.apkparse.core.model.SnapshotMeta;
import com.apkparse.core.model.UiNodeSnapshot;
import com.apkparse.core.model.UiWindowSnapshot;

final class InspectorTextFormatter {
    private InspectorTextFormatter() {
    }

    static String formatOverlaySummary(UiWindowSnapshot windowSnapshot, int nodeCount) {
        StringBuilder builder = new StringBuilder(512);
        SnapshotMeta meta = windowSnapshot == null ? null : windowSnapshot.getMeta();

        builder.append("Inspector Mode").append('\n');
        builder.append('\n');
        appendRow(builder, "package", meta == null ? null : meta.getPackageName());
        appendRow(builder, "activity", meta == null ? null : meta.getActivityName());
        appendRow(builder, "visibleNodes", String.valueOf(nodeCount));
        builder.append('\n');
        builder.append("Tap any highlighted box to inspect a node.").append('\n');
        builder.append("Use REFRESH after the target app UI changes.");
        return builder.toString();
    }

    static String formatSelection(UiWindowSnapshot windowSnapshot, UiNodeSnapshot nodeSnapshot, int x, int y) {
        StringBuilder builder = new StringBuilder(2048);
        SnapshotMeta meta = windowSnapshot == null ? null : windowSnapshot.getMeta();

        builder.append("Selected Node").append('\n');
        builder.append('\n');
        appendRow(builder, "tapPoint", x + "," + y);
        appendRow(builder, "label", buildNodeLabel(nodeSnapshot));
        appendRow(builder, "package", meta == null ? null : meta.getPackageName());
        appendRow(builder, "activity", meta == null ? null : meta.getActivityName());
        builder.append('\n');
        builder.append("[Identity]").append('\n');
        appendRow(builder, "className", nodeSnapshot.getClassName());
        appendRow(builder, "viewId", nodeSnapshot.getViewId());
        appendRow(builder, "viewIdName", nodeSnapshot.getViewIdName());
        appendRow(builder, "nodeKey", nodeSnapshot.getNodeKey());
        appendRow(builder, "parentNodeKey", nodeSnapshot.getParentNodeKey());
        builder.append('\n');
        builder.append("[Content]").append('\n');
        appendRow(builder, "content", nodeSnapshot.getContent());
        appendRow(builder, "rawText", nodeSnapshot.getRawText());
        appendRow(builder, "accessibilityText", nodeSnapshot.getAccessibilityText());
        appendRow(builder, "hintText", nodeSnapshot.getHintText());
        appendRow(builder, "tooltipText", nodeSnapshot.getTooltipText());
        appendRow(builder, "paneTitle", nodeSnapshot.getPaneTitle());
        builder.append('\n');
        builder.append("[Layout]").append('\n');
        appendRow(builder, "depth", String.valueOf(nodeSnapshot.getDepth()));
        appendRow(builder, "drawingOrderInParent", String.valueOf(nodeSnapshot.getDrawingOrderInParent()));
        appendRow(builder, "screenBounds", safe(nodeSnapshot.getScreenBounds()));
        appendRow(builder, "screenBoundsDetail", formatRect(nodeSnapshot.getScreenBoundsDetail()));
        appendRow(builder, "parentBounds", safe(nodeSnapshot.getParentBounds()));
        appendRow(builder, "parentBoundsDetail", formatRect(nodeSnapshot.getParentBoundsDetail()));
        appendRow(builder, "width", String.valueOf(nodeSnapshot.getWidth()));
        appendRow(builder, "height", String.valueOf(nodeSnapshot.getHeight()));
        appendRow(builder, "childrenCount", String.valueOf(nodeSnapshot.getChildren() == null ? 0 : nodeSnapshot.getChildren().size()));
        builder.append('\n');
        builder.append("[State]").append('\n');
        appendRow(builder, "visibleToUser", String.valueOf(nodeSnapshot.isVisibleToUser()));
        appendRow(builder, "clickable", String.valueOf(nodeSnapshot.isClickable()));
        appendRow(builder, "longClickable", String.valueOf(nodeSnapshot.isLongClickable()));
        appendRow(builder, "enabled", String.valueOf(nodeSnapshot.isEnabled()));
        appendRow(builder, "focused", String.valueOf(nodeSnapshot.isFocused()));
        appendRow(builder, "focusable", String.valueOf(nodeSnapshot.isFocusable()));
        appendRow(builder, "selected", String.valueOf(nodeSnapshot.isSelected()));
        appendRow(builder, "scrollable", String.valueOf(nodeSnapshot.isScrollable()));
        appendRow(builder, "editable", String.valueOf(nodeSnapshot.isEditable()));
        appendRow(builder, "expanded", String.valueOf(nodeSnapshot.isExpanded()));
        appendRow(builder, "password", String.valueOf(nodeSnapshot.isPassword()));
        appendRow(builder, "multiLine", String.valueOf(nodeSnapshot.isMultiLine()));
        appendRow(builder, "importantForAccessibility", String.valueOf(nodeSnapshot.isImportantForAccessibility()));
        appendRow(builder, "accessibilityHeading", String.valueOf(nodeSnapshot.isAccessibilityHeading()));
        return builder.toString();
    }

    static String buildNodeLabel(UiNodeSnapshot nodeSnapshot) {
        if (nodeSnapshot == null) {
            return "Unknown";
        }

        String viewIdName = nodeSnapshot.getViewIdName();
        if (viewIdName != null && viewIdName.length() > 0) {
            return trimLabel(viewIdName);
        }

        String content = nodeSnapshot.getContent();
        if (content != null && content.length() > 0) {
            return trimLabel(content);
        }

        return trimLabel(shortClassName(nodeSnapshot.getClassName()));
    }

    private static String formatRect(RectSnapshot rectSnapshot) {
        if (rectSnapshot == null) {
            return "-";
        }
        return rectSnapshot.getLeft() + "," + rectSnapshot.getTop() + "," + rectSnapshot.getRight() + "," + rectSnapshot.getBottom();
    }

    private static void appendRow(StringBuilder builder, String key, String value) {
        builder.append(key).append(": ").append(safe(value)).append('\n');
    }

    private static String shortClassName(String className) {
        if (className == null || className.length() == 0) {
            return "-";
        }
        int lastIndex = className.lastIndexOf('.');
        return lastIndex >= 0 && lastIndex < className.length() - 1
                ? className.substring(lastIndex + 1)
                : className;
    }

    private static String trimLabel(String value) {
        if (value == null || value.length() == 0) {
            return "-";
        }
        String normalized = value.replace('\n', ' ').trim();
        return normalized.length() > 28 ? normalized.substring(0, 25) + "..." : normalized;
    }

    private static String safe(String value) {
        return value == null || value.length() == 0 ? "-" : value;
    }
}
