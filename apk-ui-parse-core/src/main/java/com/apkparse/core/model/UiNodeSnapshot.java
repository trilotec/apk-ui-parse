package com.apkparse.core.model;

import java.util.ArrayList;
import java.util.List;

public class UiNodeSnapshot {
    private String nodeKey;
    private String parentNodeKey;
    private List<String> childNodeKeys = new ArrayList<String>();
    private int depth;
    private int siblingIndex;
    private int drawingOrderInParent;
    private String packageName;
    private String activityName;
    private String className;
    private String viewId;
    private String viewIdName;
    private String content;
    private String rawText;
    private String accessibilityText;
    private String tooltipText;
    private String hintText;
    private String paneTitle;
    private int width;
    private int height;
    private String screenBounds;
    private RectSnapshot screenBoundsDetail;
    private String parentBounds;
    private RectSnapshot parentBoundsDetail;
    private boolean visibleToUser;
    private boolean clickable;
    private boolean longClickable;
    private boolean enabled;
    private boolean focused;
    private boolean focusable;
    private boolean selected;
    private boolean scrollable;
    private boolean editable;
    private boolean expanded;
    private boolean password;
    private boolean multiLine;
    private boolean importantForAccessibility;
    private boolean accessibilityHeading;
    private List<UiNodeSnapshot> children = new ArrayList<UiNodeSnapshot>();

    public String getNodeKey() {
        return nodeKey;
    }

    public void setNodeKey(String nodeKey) {
        this.nodeKey = nodeKey;
    }

    public String getParentNodeKey() {
        return parentNodeKey;
    }

    public void setParentNodeKey(String parentNodeKey) {
        this.parentNodeKey = parentNodeKey;
    }

    public List<String> getChildNodeKeys() {
        return childNodeKeys;
    }

    public void setChildNodeKeys(List<String> childNodeKeys) {
        this.childNodeKeys = childNodeKeys;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getSiblingIndex() {
        return siblingIndex;
    }

    public void setSiblingIndex(int siblingIndex) {
        this.siblingIndex = siblingIndex;
    }

    public int getDrawingOrderInParent() {
        return drawingOrderInParent;
    }

    public void setDrawingOrderInParent(int drawingOrderInParent) {
        this.drawingOrderInParent = drawingOrderInParent;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public String getViewIdName() {
        return viewIdName;
    }

    public void setViewIdName(String viewIdName) {
        this.viewIdName = viewIdName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getAccessibilityText() {
        return accessibilityText;
    }

    public void setAccessibilityText(String accessibilityText) {
        this.accessibilityText = accessibilityText;
    }

    public String getTooltipText() {
        return tooltipText;
    }

    public void setTooltipText(String tooltipText) {
        this.tooltipText = tooltipText;
    }

    public String getHintText() {
        return hintText;
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
    }

    public String getPaneTitle() {
        return paneTitle;
    }

    public void setPaneTitle(String paneTitle) {
        this.paneTitle = paneTitle;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getScreenBounds() {
        return screenBounds;
    }

    public void setScreenBounds(String screenBounds) {
        this.screenBounds = screenBounds;
    }

    public RectSnapshot getScreenBoundsDetail() {
        return screenBoundsDetail;
    }

    public void setScreenBoundsDetail(RectSnapshot screenBoundsDetail) {
        this.screenBoundsDetail = screenBoundsDetail;
    }

    public String getParentBounds() {
        return parentBounds;
    }

    public void setParentBounds(String parentBounds) {
        this.parentBounds = parentBounds;
    }

    public RectSnapshot getParentBoundsDetail() {
        return parentBoundsDetail;
    }

    public void setParentBoundsDetail(RectSnapshot parentBoundsDetail) {
        this.parentBoundsDetail = parentBoundsDetail;
    }

    public boolean isVisibleToUser() {
        return visibleToUser;
    }

    public void setVisibleToUser(boolean visibleToUser) {
        this.visibleToUser = visibleToUser;
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    public boolean isLongClickable() {
        return longClickable;
    }

    public void setLongClickable(boolean longClickable) {
        this.longClickable = longClickable;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean isFocusable() {
        return focusable;
    }

    public void setFocusable(boolean focusable) {
        this.focusable = focusable;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isScrollable() {
        return scrollable;
    }

    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isPassword() {
        return password;
    }

    public void setPassword(boolean password) {
        this.password = password;
    }

    public boolean isMultiLine() {
        return multiLine;
    }

    public void setMultiLine(boolean multiLine) {
        this.multiLine = multiLine;
    }

    public boolean isImportantForAccessibility() {
        return importantForAccessibility;
    }

    public void setImportantForAccessibility(boolean importantForAccessibility) {
        this.importantForAccessibility = importantForAccessibility;
    }

    public boolean isAccessibilityHeading() {
        return accessibilityHeading;
    }

    public void setAccessibilityHeading(boolean accessibilityHeading) {
        this.accessibilityHeading = accessibilityHeading;
    }

    public List<UiNodeSnapshot> getChildren() {
        return children;
    }

    public void setChildren(List<UiNodeSnapshot> children) {
        this.children = children;
    }
}
