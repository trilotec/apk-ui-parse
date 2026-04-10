package com.apkparse.android.mapper;

import android.graphics.Rect;
import android.os.Build;
import android.view.accessibility.AccessibilityNodeInfo;

import com.apkparse.android.capture.ForegroundWindowSnapshot;
import com.apkparse.core.model.DumpOptions;
import com.apkparse.core.model.RectSnapshot;
import com.apkparse.core.model.UiNodeSnapshot;
import com.apkparse.core.util.ViewIdParser;

public class NodeFieldMapper {
    public UiNodeSnapshot map(
            AccessibilityNodeInfo node,
            AccessibilityNodeInfo parent,
            String nodeKey,
            String parentNodeKey,
            int depth,
            int siblingIndex,
            ForegroundWindowSnapshot foregroundWindowSnapshot,
            DumpOptions dumpOptions
    ) {
        Rect screenRect = new Rect();
        node.getBoundsInScreen(screenRect);

        Rect parentScreenRect = new Rect();
        if (parent == null) {
            parentScreenRect.set(screenRect);
        } else {
            parent.getBoundsInScreen(parentScreenRect);
        }

        RectSnapshot screenBoundsDetail = new RectSnapshot(screenRect.left, screenRect.top, screenRect.right, screenRect.bottom);
        RectSnapshot parentBoundsDetail = parent == null
                ? new RectSnapshot(screenRect.left, screenRect.top, screenRect.right, screenRect.bottom)
                : new RectSnapshot(
                        screenRect.left - parentScreenRect.left,
                        screenRect.top - parentScreenRect.top,
                        screenRect.right - parentScreenRect.left,
                        screenRect.bottom - parentScreenRect.top
                );

        String rawText = toStringValue(node.getText());
        String accessibilityText = toStringValue(node.getContentDescription());
        String hintText = Build.VERSION.SDK_INT >= 26 ? toStringValue(node.getHintText()) : null;
        String tooltipText = Build.VERSION.SDK_INT >= 28 ? toStringValue(node.getTooltipText()) : null;
        String paneTitle = Build.VERSION.SDK_INT >= 28 ? toStringValue(node.getPaneTitle()) : null;
        String stateDescription = Build.VERSION.SDK_INT >= 30 ? toStringValue(node.getStateDescription()) : null;
        boolean password = node.isPassword();

        if (password && dumpOptions.isMaskPasswordText()) {
            rawText = mask(rawText);
            accessibilityText = mask(accessibilityText);
        }

        UiNodeSnapshot snapshot = new UiNodeSnapshot();
        snapshot.setNodeKey(nodeKey);
        snapshot.setParentNodeKey(parentNodeKey);
        snapshot.setDepth(depth);
        snapshot.setSiblingIndex(siblingIndex);
        snapshot.setDrawingOrderInParent(Build.VERSION.SDK_INT >= 24 ? node.getDrawingOrder() : siblingIndex);
        snapshot.setPackageName(firstNonEmpty(toStringValue(node.getPackageName()), foregroundWindowSnapshot.getPackageName()));
        snapshot.setActivityName(foregroundWindowSnapshot.getActivityName());
        snapshot.setClassName(toStringValue(node.getClassName()));
        snapshot.setViewId(node.getViewIdResourceName());
        snapshot.setViewIdName(ViewIdParser.extractViewIdName(node.getViewIdResourceName()));
        snapshot.setRawText(dumpOptions.isIncludeRawText() ? rawText : null);
        snapshot.setAccessibilityText(accessibilityText);
        snapshot.setHintText(hintText);
        snapshot.setTooltipText(tooltipText);
        snapshot.setPaneTitle(paneTitle);
        snapshot.setContent(resolveContent(rawText, accessibilityText, hintText, stateDescription, password, dumpOptions));
        snapshot.setScreenBounds(screenBoundsDetail.toCompactString());
        snapshot.setScreenBoundsDetail(screenBoundsDetail);
        snapshot.setParentBounds(parentBoundsDetail.toCompactString());
        snapshot.setParentBoundsDetail(parentBoundsDetail);
        snapshot.setWidth(screenBoundsDetail.getWidth());
        snapshot.setHeight(screenBoundsDetail.getHeight());
        snapshot.setVisibleToUser(node.isVisibleToUser());
        snapshot.setClickable(node.isClickable());
        snapshot.setLongClickable(node.isLongClickable());
        snapshot.setEnabled(node.isEnabled());
        snapshot.setFocused(node.isFocused());
        snapshot.setFocusable(node.isFocusable());
        snapshot.setSelected(node.isSelected());
        snapshot.setScrollable(node.isScrollable());
        snapshot.setEditable(Build.VERSION.SDK_INT >= 18 && node.isEditable());
        snapshot.setExpanded(resolveExpanded(node));
        snapshot.setPassword(password);
        snapshot.setMultiLine(Build.VERSION.SDK_INT >= 19 && node.isMultiLine());
        snapshot.setImportantForAccessibility(Build.VERSION.SDK_INT >= 24 && node.isImportantForAccessibility());
        snapshot.setAccessibilityHeading(Build.VERSION.SDK_INT >= 28 && node.isHeading());
        return snapshot;
    }

    private static String resolveContent(String rawText, String accessibilityText, String hintText, String stateDescription, boolean password, DumpOptions dumpOptions) {
        String value = firstNonEmpty(rawText, accessibilityText, hintText, stateDescription);
        if (password && dumpOptions.isMaskPasswordText()) {
            return mask(value);
        }
        return value;
    }

    private static String mask(String value) {
        if (value == null || value.length() == 0) {
            return value;
        }
        return "******";
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && value.length() > 0) {
                return value;
            }
        }
        return null;
    }

    private static String toStringValue(CharSequence value) {
        return value == null ? null : value.toString();
    }

    private static boolean resolveExpanded(AccessibilityNodeInfo node) {
        if (Build.VERSION.SDK_INT >= 21) {
            java.util.List<AccessibilityNodeInfo.AccessibilityAction> actions = node.getActionList();
            if (actions != null) {
                for (AccessibilityNodeInfo.AccessibilityAction action : actions) {
                    if (action == null) {
                        continue;
                    }
                    if (action.getId() == AccessibilityNodeInfo.AccessibilityAction.ACTION_COLLAPSE.getId()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
