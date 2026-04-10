package com.apkparse.android.capture;

import android.view.accessibility.AccessibilityNodeInfo;

import com.apkparse.android.mapper.NodeFieldMapper;
import com.apkparse.core.model.DumpOptions;
import com.apkparse.core.model.UiNodeSnapshot;

public class NodeTreeWalker {
    private final NodeFieldMapper nodeFieldMapper;
    private final DumpOptions dumpOptions;
    private final ForegroundWindowSnapshot foregroundWindowSnapshot;
    private int visitedNodeCount;

    public NodeTreeWalker(NodeFieldMapper nodeFieldMapper, DumpOptions dumpOptions, ForegroundWindowSnapshot foregroundWindowSnapshot) {
        this.nodeFieldMapper = nodeFieldMapper;
        this.dumpOptions = dumpOptions;
        this.foregroundWindowSnapshot = foregroundWindowSnapshot;
    }

    public UiNodeSnapshot walk(AccessibilityNodeInfo root) {
        return walkInternal(root, null, "0", null, 0, 0);
    }

    private UiNodeSnapshot walkInternal(
            AccessibilityNodeInfo node,
            AccessibilityNodeInfo parent,
            String nodeKey,
            String parentNodeKey,
            int depth,
            int siblingIndex
    ) {
        if (node == null || depth > dumpOptions.getMaxDepth() || visitedNodeCount >= dumpOptions.getMaxNodeCount()) {
            return null;
        }

        if (!dumpOptions.isIncludeInvisibleNodes() && !node.isVisibleToUser()) {
            return null;
        }

        visitedNodeCount++;
        UiNodeSnapshot snapshot = nodeFieldMapper.map(
                node,
                parent,
                nodeKey,
                parentNodeKey,
                depth,
                siblingIndex,
                foregroundWindowSnapshot,
                dumpOptions
        );

        if (!dumpOptions.isIncludeChildren()) {
            return snapshot;
        }

        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) {
                continue;
            }

            try {
                String childNodeKey = nodeKey + "." + i;
                UiNodeSnapshot childSnapshot = walkInternal(child, node, childNodeKey, nodeKey, depth + 1, i);
                if (childSnapshot != null) {
                    snapshot.getChildNodeKeys().add(childNodeKey);
                    snapshot.getChildren().add(childSnapshot);
                }
            } finally {
                recycleIfNeeded(child);
            }
        }

        return snapshot;
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
