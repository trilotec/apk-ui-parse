package com.apkparse.core.model;

public class DumpOptions {
    private final boolean includeInvisibleNodes;
    private final int maxDepth;
    private final int maxNodeCount;
    private final boolean includeChildren;
    private final boolean includeRawText;
    private final boolean prettyJson;
    private final boolean maskPasswordText;

    private DumpOptions(Builder builder) {
        this.includeInvisibleNodes = builder.includeInvisibleNodes;
        this.maxDepth = builder.maxDepth;
        this.maxNodeCount = builder.maxNodeCount;
        this.includeChildren = builder.includeChildren;
        this.includeRawText = builder.includeRawText;
        this.prettyJson = builder.prettyJson;
        this.maskPasswordText = builder.maskPasswordText;
    }

    public static DumpOptions defaults() {
        return new Builder().build();
    }

    public boolean isIncludeInvisibleNodes() {
        return includeInvisibleNodes;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public int getMaxNodeCount() {
        return maxNodeCount;
    }

    public boolean isIncludeChildren() {
        return includeChildren;
    }

    public boolean isIncludeRawText() {
        return includeRawText;
    }

    public boolean isPrettyJson() {
        return prettyJson;
    }

    public boolean isMaskPasswordText() {
        return maskPasswordText;
    }

    public static final class Builder {
        private boolean includeInvisibleNodes;
        private int maxDepth = 100;
        private int maxNodeCount = 3000;
        private boolean includeChildren = true;
        private boolean includeRawText = true;
        private boolean prettyJson = true;
        private boolean maskPasswordText = true;

        public Builder includeInvisibleNodes(boolean includeInvisibleNodes) {
            this.includeInvisibleNodes = includeInvisibleNodes;
            return this;
        }

        public Builder maxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }

        public Builder maxNodeCount(int maxNodeCount) {
            this.maxNodeCount = maxNodeCount;
            return this;
        }

        public Builder includeChildren(boolean includeChildren) {
            this.includeChildren = includeChildren;
            return this;
        }

        public Builder includeRawText(boolean includeRawText) {
            this.includeRawText = includeRawText;
            return this;
        }

        public Builder prettyJson(boolean prettyJson) {
            this.prettyJson = prettyJson;
            return this;
        }

        public Builder maskPasswordText(boolean maskPasswordText) {
            this.maskPasswordText = maskPasswordText;
            return this;
        }

        public DumpOptions build() {
            return new DumpOptions(this);
        }
    }
}
