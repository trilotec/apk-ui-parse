package com.apkparse.core.export;

import com.apkparse.core.model.RectSnapshot;
import com.apkparse.core.model.SnapshotMeta;
import com.apkparse.core.model.UiNodeSnapshot;
import com.apkparse.core.model.UiWindowSnapshot;

import java.util.Iterator;
import java.util.List;

public final class JsonExporter {
    private JsonExporter() {
    }

    public static String toJson(UiWindowSnapshot snapshot, boolean pretty) {
        StringBuilder builder = new StringBuilder(8192);
        writeWindowSnapshot(builder, snapshot, pretty, 0);
        return builder.toString();
    }

    private static void writeWindowSnapshot(StringBuilder builder, UiWindowSnapshot snapshot, boolean pretty, int indent) {
        builder.append('{');
        newline(builder, pretty, indent + 1);
        builder.append(quote("meta")).append(':');
        space(builder, pretty);
        writeMeta(builder, snapshot.getMeta(), pretty, indent + 1);
        builder.append(',');
        newline(builder, pretty, indent + 1);
        builder.append(quote("root")).append(':');
        space(builder, pretty);
        writeNode(builder, snapshot.getRoot(), pretty, indent + 1);
        newline(builder, pretty, indent);
        builder.append('}');
    }

    private static void writeMeta(StringBuilder builder, SnapshotMeta meta, boolean pretty, int indent) {
        builder.append('{');
        appendField(builder, "schemaVersion", meta.getSchemaVersion(), pretty, indent, true);
        appendField(builder, "captureTime", meta.getCaptureTime(), pretty, indent, true);
        appendField(builder, "packageName", meta.getPackageName(), pretty, indent, true);
        appendField(builder, "activityName", meta.getActivityName(), pretty, indent, true);
        appendField(builder, "windowId", meta.getWindowId(), pretty, indent, true);
        appendField(builder, "screenWidth", meta.getScreenWidth(), pretty, indent, true);
        appendField(builder, "screenHeight", meta.getScreenHeight(), pretty, indent, true);
        appendField(builder, "source", meta.getSource(), pretty, indent, false);
        newline(builder, pretty, indent - 1);
        builder.append('}');
    }

    private static void writeNode(StringBuilder builder, UiNodeSnapshot node, boolean pretty, int indent) {
        builder.append('{');
        appendField(builder, "nodeKey", node.getNodeKey(), pretty, indent, true);
        appendField(builder, "parentNodeKey", node.getParentNodeKey(), pretty, indent, true);
        appendStringArray(builder, "childNodeKeys", node.getChildNodeKeys(), pretty, indent, true);
        appendField(builder, "depth", node.getDepth(), pretty, indent, true);
        appendField(builder, "siblingIndex", node.getSiblingIndex(), pretty, indent, true);
        appendField(builder, "drawingOrderInParent", node.getDrawingOrderInParent(), pretty, indent, true);
        appendField(builder, "packageName", node.getPackageName(), pretty, indent, true);
        appendField(builder, "activityName", node.getActivityName(), pretty, indent, true);
        appendField(builder, "className", node.getClassName(), pretty, indent, true);
        appendField(builder, "viewId", node.getViewId(), pretty, indent, true);
        appendField(builder, "viewIdName", node.getViewIdName(), pretty, indent, true);
        appendField(builder, "content", node.getContent(), pretty, indent, true);
        appendField(builder, "rawText", node.getRawText(), pretty, indent, true);
        appendField(builder, "accessibilityText", node.getAccessibilityText(), pretty, indent, true);
        appendField(builder, "tooltipText", node.getTooltipText(), pretty, indent, true);
        appendField(builder, "hintText", node.getHintText(), pretty, indent, true);
        appendField(builder, "paneTitle", node.getPaneTitle(), pretty, indent, true);
        appendField(builder, "width", node.getWidth(), pretty, indent, true);
        appendField(builder, "height", node.getHeight(), pretty, indent, true);
        appendField(builder, "screenBounds", node.getScreenBounds(), pretty, indent, true);
        appendRect(builder, "screenBoundsDetail", node.getScreenBoundsDetail(), pretty, indent, true);
        appendField(builder, "parentBounds", node.getParentBounds(), pretty, indent, true);
        appendRect(builder, "parentBoundsDetail", node.getParentBoundsDetail(), pretty, indent, true);
        appendField(builder, "visibleToUser", node.isVisibleToUser(), pretty, indent, true);
        appendField(builder, "clickable", node.isClickable(), pretty, indent, true);
        appendField(builder, "longClickable", node.isLongClickable(), pretty, indent, true);
        appendField(builder, "enabled", node.isEnabled(), pretty, indent, true);
        appendField(builder, "focused", node.isFocused(), pretty, indent, true);
        appendField(builder, "focusable", node.isFocusable(), pretty, indent, true);
        appendField(builder, "selected", node.isSelected(), pretty, indent, true);
        appendField(builder, "scrollable", node.isScrollable(), pretty, indent, true);
        appendField(builder, "editable", node.isEditable(), pretty, indent, true);
        appendField(builder, "expanded", node.isExpanded(), pretty, indent, true);
        appendField(builder, "password", node.isPassword(), pretty, indent, true);
        appendField(builder, "multiLine", node.isMultiLine(), pretty, indent, true);
        appendField(builder, "importantForAccessibility", node.isImportantForAccessibility(), pretty, indent, true);
        appendField(builder, "accessibilityHeading", node.isAccessibilityHeading(), pretty, indent, true);
        appendNodeArray(builder, "children", node.getChildren(), pretty, indent, false);
        newline(builder, pretty, indent - 1);
        builder.append('}');
    }

    private static void appendRect(StringBuilder builder, String name, RectSnapshot rect, boolean pretty, int indent, boolean comma) {
        newline(builder, pretty, indent);
        builder.append(quote(name)).append(':');
        space(builder, pretty);
        if (rect == null) {
            builder.append("null");
        } else {
            builder.append('{');
            appendField(builder, "left", rect.getLeft(), pretty, indent + 1, true);
            appendField(builder, "top", rect.getTop(), pretty, indent + 1, true);
            appendField(builder, "right", rect.getRight(), pretty, indent + 1, true);
            appendField(builder, "bottom", rect.getBottom(), pretty, indent + 1, false);
            newline(builder, pretty, indent);
            builder.append('}');
        }
        if (comma) {
            builder.append(',');
        }
    }

    private static void appendStringArray(StringBuilder builder, String name, List<String> values, boolean pretty, int indent, boolean comma) {
        newline(builder, pretty, indent);
        builder.append(quote(name)).append(':');
        space(builder, pretty);
        builder.append('[');
        if (values != null && !values.isEmpty()) {
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    builder.append(',');
                    space(builder, pretty);
                }
                builder.append(quote(values.get(i)));
            }
        }
        builder.append(']');
        if (comma) {
            builder.append(',');
        }
    }

    private static void appendNodeArray(StringBuilder builder, String name, List<UiNodeSnapshot> values, boolean pretty, int indent, boolean comma) {
        newline(builder, pretty, indent);
        builder.append(quote(name)).append(':');
        space(builder, pretty);
        builder.append('[');
        if (values != null && !values.isEmpty()) {
            Iterator<UiNodeSnapshot> iterator = values.iterator();
            int childIndent = indent + 1;
            while (iterator.hasNext()) {
                newline(builder, pretty, childIndent);
                writeNode(builder, iterator.next(), pretty, childIndent);
                if (iterator.hasNext()) {
                    builder.append(',');
                }
            }
            newline(builder, pretty, indent);
        }
        builder.append(']');
        if (comma) {
            builder.append(',');
        }
    }

    private static void appendField(StringBuilder builder, String name, String value, boolean pretty, int indent, boolean comma) {
        newline(builder, pretty, indent);
        builder.append(quote(name)).append(':');
        space(builder, pretty);
        if (value == null) {
            builder.append("null");
        } else {
            builder.append(quote(value));
        }
        if (comma) {
            builder.append(',');
        }
    }

    private static void appendField(StringBuilder builder, String name, long value, boolean pretty, int indent, boolean comma) {
        newline(builder, pretty, indent);
        builder.append(quote(name)).append(':');
        space(builder, pretty);
        builder.append(value);
        if (comma) {
            builder.append(',');
        }
    }

    private static void appendField(StringBuilder builder, String name, int value, boolean pretty, int indent, boolean comma) {
        newline(builder, pretty, indent);
        builder.append(quote(name)).append(':');
        space(builder, pretty);
        builder.append(value);
        if (comma) {
            builder.append(',');
        }
    }

    private static void appendField(StringBuilder builder, String name, boolean value, boolean pretty, int indent, boolean comma) {
        newline(builder, pretty, indent);
        builder.append(quote(name)).append(':');
        space(builder, pretty);
        builder.append(value);
        if (comma) {
            builder.append(',');
        }
    }

    private static String quote(String value) {
        return "\"" + escape(value) + "\"";
    }

    private static String escape(String value) {
        StringBuilder builder = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\':
                    builder.append("\\\\");
                    break;
                case '"':
                    builder.append("\\\"");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        builder.append(String.format("\\u%04x", (int) c));
                    } else {
                        builder.append(c);
                    }
                    break;
            }
        }
        return builder.toString();
    }

    private static void newline(StringBuilder builder, boolean pretty, int indent) {
        if (!pretty) {
            return;
        }
        builder.append('\n');
        for (int i = 0; i < indent; i++) {
            builder.append("  ");
        }
    }

    private static void space(StringBuilder builder, boolean pretty) {
        if (pretty) {
            builder.append(' ');
        }
    }
}
