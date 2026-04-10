package com.apkparse.sample;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apkparse.core.model.RectSnapshot;
import com.apkparse.core.model.SnapshotMeta;
import com.apkparse.core.model.UiNodeSnapshot;
import com.apkparse.core.model.UiWindowSnapshot;

final class InspectorPanelRenderer {
    private InspectorPanelRenderer() {
    }

    static void bindSummary(Context context, LinearLayout container, UiWindowSnapshot windowSnapshot, int nodeCount) {
        container.removeAllViews();

        SnapshotMeta meta = windowSnapshot == null ? null : windowSnapshot.getMeta();
        LinearLayout appSection = createSection(context, "Foreground App");
        addRow(context, appSection, "Package", meta == null ? null : meta.getPackageName());
        addRow(context, appSection, "Activity", meta == null ? null : meta.getActivityName());
        addRow(context, appSection, "Visible Nodes", String.valueOf(nodeCount));
        addRow(context, appSection, "Window Id", meta == null ? null : String.valueOf(meta.getWindowId()));
        addRow(
                context,
                appSection,
                "Screen",
                meta == null ? null : meta.getScreenWidth() + " x " + meta.getScreenHeight()
        );
        container.addView(appSection);

        LinearLayout guideSection = createSection(context, "How To Use");
        addRow(context, guideSection, "Step 1", "Tap a highlighted box to inspect a node.");
        addRow(context, guideSection, "Step 2", "Tap REFRESH after the target app UI changes.");
        addRow(context, guideSection, "Cross-App", "Switch to any app, then tap the floating ball again.");
        container.addView(guideSection);
    }

    static void bindSelection(Context context, LinearLayout container, UiWindowSnapshot windowSnapshot, UiNodeSnapshot nodeSnapshot, int x, int y) {
        container.removeAllViews();

        SnapshotMeta meta = windowSnapshot == null ? null : windowSnapshot.getMeta();

        LinearLayout targetSection = createSection(context, "Target");
        addRow(context, targetSection, "Tap Point", x + ", " + y);
        addRow(context, targetSection, "Label", InspectorTextFormatter.buildNodeLabel(nodeSnapshot));
        addRow(context, targetSection, "Package", meta == null ? null : meta.getPackageName());
        addRow(context, targetSection, "Activity", meta == null ? null : meta.getActivityName());
        container.addView(targetSection);

        LinearLayout identitySection = createSection(context, "Identity");
        addRow(context, identitySection, "Class", nodeSnapshot.getClassName());
        addRow(context, identitySection, "View ID", nodeSnapshot.getViewId());
        addRow(context, identitySection, "View ID Name", nodeSnapshot.getViewIdName());
        addRow(context, identitySection, "Node Key", nodeSnapshot.getNodeKey());
        addRow(context, identitySection, "Parent Node", nodeSnapshot.getParentNodeKey());
        addRow(context, identitySection, "Parent Class", findParentLabel(windowSnapshot, nodeSnapshot));
        addRow(context, identitySection, "Child Count", String.valueOf(nodeSnapshot.getChildren() == null ? 0 : nodeSnapshot.getChildren().size()));
        container.addView(identitySection);

        LinearLayout contentSection = createSection(context, "Content");
        addRow(context, contentSection, "Content", nodeSnapshot.getContent());
        addRow(context, contentSection, "Raw Text", nodeSnapshot.getRawText());
        addRow(context, contentSection, "A11y Text", nodeSnapshot.getAccessibilityText());
        addRow(context, contentSection, "Hint", nodeSnapshot.getHintText());
        addRow(context, contentSection, "Tooltip", nodeSnapshot.getTooltipText());
        addRow(context, contentSection, "Pane Title", nodeSnapshot.getPaneTitle());
        container.addView(contentSection);

        LinearLayout layoutSection = createSection(context, "Layout");
        addRow(context, layoutSection, "Depth", String.valueOf(nodeSnapshot.getDepth()));
        addRow(context, layoutSection, "Sibling Index", String.valueOf(nodeSnapshot.getSiblingIndex()));
        addRow(context, layoutSection, "Draw Order", String.valueOf(nodeSnapshot.getDrawingOrderInParent()));
        addRow(context, layoutSection, "Width", String.valueOf(nodeSnapshot.getWidth()));
        addRow(context, layoutSection, "Height", String.valueOf(nodeSnapshot.getHeight()));
        addRow(context, layoutSection, "Screen Bounds", nodeSnapshot.getScreenBounds());
        addRow(context, layoutSection, "Screen Detail", formatRect(nodeSnapshot.getScreenBoundsDetail()));
        addRow(context, layoutSection, "Parent Bounds", nodeSnapshot.getParentBounds());
        addRow(context, layoutSection, "Parent Detail", formatRect(nodeSnapshot.getParentBoundsDetail()));
        container.addView(layoutSection);

        LinearLayout stateSection = createSection(context, "State");
        addBooleanRow(context, stateSection, "Visible", nodeSnapshot.isVisibleToUser());
        addBooleanRow(context, stateSection, "Clickable", nodeSnapshot.isClickable());
        addBooleanRow(context, stateSection, "Long Clickable", nodeSnapshot.isLongClickable());
        addBooleanRow(context, stateSection, "Enabled", nodeSnapshot.isEnabled());
        addBooleanRow(context, stateSection, "Focused", nodeSnapshot.isFocused());
        addBooleanRow(context, stateSection, "Focusable", nodeSnapshot.isFocusable());
        addBooleanRow(context, stateSection, "Selected", nodeSnapshot.isSelected());
        addBooleanRow(context, stateSection, "Scrollable", nodeSnapshot.isScrollable());
        addBooleanRow(context, stateSection, "Editable", nodeSnapshot.isEditable());
        addBooleanRow(context, stateSection, "Expanded", nodeSnapshot.isExpanded());
        addBooleanRow(context, stateSection, "Password", nodeSnapshot.isPassword());
        addBooleanRow(context, stateSection, "Multi Line", nodeSnapshot.isMultiLine());
        addBooleanRow(context, stateSection, "Important For A11y", nodeSnapshot.isImportantForAccessibility());
        addBooleanRow(context, stateSection, "A11y Heading", nodeSnapshot.isAccessibilityHeading());
        container.addView(stateSection);
    }

    private static String findParentLabel(UiWindowSnapshot windowSnapshot, UiNodeSnapshot nodeSnapshot) {
        if (windowSnapshot == null || windowSnapshot.getRoot() == null || nodeSnapshot == null) {
            return "-";
        }
        UiNodeSnapshot parentNode = findNodeByKey(windowSnapshot.getRoot(), nodeSnapshot.getParentNodeKey());
        if (parentNode == null) {
            return "-";
        }
        return InspectorTextFormatter.buildNodeLabel(parentNode) + " (" + safe(parentNode.getClassName()) + ")";
    }

    private static UiNodeSnapshot findNodeByKey(UiNodeSnapshot currentNode, String nodeKey) {
        if (currentNode == null || TextUtils.isEmpty(nodeKey)) {
            return null;
        }
        if (nodeKey.equals(currentNode.getNodeKey())) {
            return currentNode;
        }
        if (currentNode.getChildren() == null) {
            return null;
        }
        for (UiNodeSnapshot childNode : currentNode.getChildren()) {
            UiNodeSnapshot match = findNodeByKey(childNode, nodeKey);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    private static LinearLayout createSection(Context context, String title) {
        LinearLayout section = new LinearLayout(context);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(dp(context, 12), dp(context, 10), dp(context, 12), dp(context, 10));
        section.setBackground(createSectionBackground());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(context, 10);
        section.setLayoutParams(params);

        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextColor(0xFFB3E5FC);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setPadding(0, 0, 0, dp(context, 8));
        section.addView(titleView);
        return section;
    }

    private static void addBooleanRow(Context context, LinearLayout section, String key, boolean value) {
        addRow(context, section, key, value ? "true" : "false");
    }

    private static void addRow(Context context, LinearLayout section, String key, String value) {
        if (section.getChildCount() > 1) {
            section.addView(createDivider(context));
        }

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        row.setPadding(0, dp(context, 6), 0, dp(context, 6));

        TextView keyView = new TextView(context);
        keyView.setText(key);
        keyView.setTextColor(0xFF90A4AE);
        keyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        keyView.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams keyParams = new LinearLayout.LayoutParams(dp(context, 114), LinearLayout.LayoutParams.WRAP_CONTENT);
        row.addView(keyView, keyParams);

        TextView valueView = new TextView(context);
        valueView.setText(safe(value));
        valueView.setTextColor(0xFFF4F7FB);
        valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        valueView.setLineSpacing(0f, 1.1f);
        valueView.setTextIsSelectable(true);
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(valueView, valueParams);

        section.addView(row);
    }

    private static View createDivider(Context context) {
        View divider = new View(context);
        divider.setBackgroundColor(0x223E556B);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                Math.max(1, dp(context, 1))
        ));
        return divider;
    }

    private static GradientDrawable createSectionBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(0x221F2A3B);
        drawable.setCornerRadius(20f);
        drawable.setStroke(1, 0x223F637D);
        return drawable;
    }

    private static String formatRect(RectSnapshot rectSnapshot) {
        if (rectSnapshot == null) {
            return "-";
        }
        return "[" + rectSnapshot.getLeft() + ", " + rectSnapshot.getTop() + "] - ["
                + rectSnapshot.getRight() + ", " + rectSnapshot.getBottom() + "]";
    }

    private static String safe(String value) {
        return value == null || value.length() == 0 ? "-" : value;
    }

    private static int dp(Context context, int value) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.max(1, (int) (value * density));
    }
}
