package com.apkparse.core.model;

public class UiWindowSnapshot {
    private SnapshotMeta meta;
    private UiNodeSnapshot root;

    public SnapshotMeta getMeta() {
        return meta;
    }

    public void setMeta(SnapshotMeta meta) {
        this.meta = meta;
    }

    public UiNodeSnapshot getRoot() {
        return root;
    }

    public void setRoot(UiNodeSnapshot root) {
        this.root = root;
    }
}
