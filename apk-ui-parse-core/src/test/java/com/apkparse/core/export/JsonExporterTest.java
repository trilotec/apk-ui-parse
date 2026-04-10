package com.apkparse.core.export;

import com.apkparse.core.model.RectSnapshot;
import com.apkparse.core.model.SnapshotMeta;
import com.apkparse.core.model.UiNodeSnapshot;
import com.apkparse.core.model.UiWindowSnapshot;

import org.junit.Assert;
import org.junit.Test;

public class JsonExporterTest {
    @Test
    public void toJson_containsExpectedFields() {
        SnapshotMeta meta = new SnapshotMeta();
        meta.setSchemaVersion("1.0.0");
        meta.setCaptureTime(123L);
        meta.setPackageName("com.demo.app");
        meta.setActivityName("com.demo.MainActivity");
        meta.setWindowId(7);
        meta.setScreenWidth(1080);
        meta.setScreenHeight(2400);
        meta.setSource("accessibility");

        UiNodeSnapshot root = new UiNodeSnapshot();
        root.setNodeKey("0");
        root.setDepth(0);
        root.setSiblingIndex(0);
        root.setClassName("android.widget.TextView");
        root.setViewId("com.demo:id/title");
        root.setViewIdName("title");
        root.setContent("Hello");
        root.setScreenBounds("0,0,100,40");
        root.setScreenBoundsDetail(new RectSnapshot(0, 0, 100, 40));
        root.setParentBounds("0,0,100,40");
        root.setParentBoundsDetail(new RectSnapshot(0, 0, 100, 40));
        root.setWidth(100);
        root.setHeight(40);
        root.setVisibleToUser(true);
        root.setEnabled(true);

        UiWindowSnapshot snapshot = new UiWindowSnapshot();
        snapshot.setMeta(meta);
        snapshot.setRoot(root);

        String json = JsonExporter.toJson(snapshot, false);

        Assert.assertTrue(json.contains("\"schemaVersion\":\"1.0.0\""));
        Assert.assertTrue(json.contains("\"packageName\":\"com.demo.app\""));
        Assert.assertTrue(json.contains("\"className\":\"android.widget.TextView\""));
        Assert.assertTrue(json.contains("\"viewIdName\":\"title\""));
        Assert.assertTrue(json.contains("\"screenBounds\":\"0,0,100,40\""));
        Assert.assertTrue(json.contains("\"children\":[]"));
    }
}
