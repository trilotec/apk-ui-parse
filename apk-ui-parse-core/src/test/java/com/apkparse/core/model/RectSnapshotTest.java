package com.apkparse.core.model;

import org.junit.Assert;
import org.junit.Test;

public class RectSnapshotTest {
    @Test
    public void rectSnapshot_calculatesWidthHeightAndCompactString() {
        RectSnapshot rectSnapshot = new RectSnapshot(10, 20, 110, 220);

        Assert.assertEquals(100, rectSnapshot.getWidth());
        Assert.assertEquals(200, rectSnapshot.getHeight());
        Assert.assertEquals("10,20,110,220", rectSnapshot.toCompactString());
    }
}
