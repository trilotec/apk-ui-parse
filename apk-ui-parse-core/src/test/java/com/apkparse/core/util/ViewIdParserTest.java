package com.apkparse.core.util;

import org.junit.Assert;
import org.junit.Test;

public class ViewIdParserTest {
    @Test
    public void extractViewIdName_returnsTailSegment() {
        Assert.assertEquals("login_btn", ViewIdParser.extractViewIdName("com.demo:id/login_btn"));
    }

    @Test
    public void extractViewIdName_returnsNullForNullInput() {
        Assert.assertNull(ViewIdParser.extractViewIdName(null));
    }

    @Test
    public void extractViewIdName_returnsOriginalWhenNoSeparatorExists() {
        Assert.assertEquals("plain_id", ViewIdParser.extractViewIdName("plain_id"));
    }
}
