package com.apkparse.sample;

import com.apkparse.core.model.RectSnapshot;
import com.apkparse.core.model.UiNodeSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class NodeHitTestHelper {
    private NodeHitTestHelper() {
    }

    static UiNodeSnapshot findDeepestNodeAt(UiNodeSnapshot root, int x, int y) {
        if (root == null || !contains(root.getScreenBoundsDetail(), x, y)) {
            return null;
        }

        List<UiNodeSnapshot> children = root.getChildren();
        if (children != null) {
            for (int i = children.size() - 1; i >= 0; i--) {
                UiNodeSnapshot child = children.get(i);
                UiNodeSnapshot match = findDeepestNodeAt(child, x, y);
                if (match != null) {
                    return match;
                }
            }
        }
        return root;
    }

    static List<UiNodeSnapshot> flattenVisibleNodes(UiNodeSnapshot root) {
        if (root == null) {
            return Collections.emptyList();
        }

        List<UiNodeSnapshot> nodes = new ArrayList<UiNodeSnapshot>();
        collectVisibleNodes(root, nodes);
        return nodes;
    }

    private static void collectVisibleNodes(UiNodeSnapshot node, List<UiNodeSnapshot> nodes) {
        if (node == null) {
            return;
        }

        RectSnapshot rect = node.getScreenBoundsDetail();
        if (node.isVisibleToUser() && rect != null && rect.getWidth() > 1 && rect.getHeight() > 1) {
            nodes.add(node);
        }

        List<UiNodeSnapshot> children = node.getChildren();
        if (children == null) {
            return;
        }

        for (UiNodeSnapshot child : children) {
            collectVisibleNodes(child, nodes);
        }
    }

    private static boolean contains(RectSnapshot rect, int x, int y) {
        if (rect == null) {
            return false;
        }
        return x >= rect.getLeft()
                && x <= rect.getRight()
                && y >= rect.getTop()
                && y <= rect.getBottom();
    }
}
