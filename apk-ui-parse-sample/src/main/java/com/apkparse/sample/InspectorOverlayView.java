package com.apkparse.sample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.apkparse.core.model.RectSnapshot;
import com.apkparse.core.model.UiNodeSnapshot;
import com.apkparse.core.model.UiWindowSnapshot;

import java.util.Collections;
import java.util.List;

public class InspectorOverlayView extends View {
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private UiWindowSnapshot snapshot;
    private List<UiNodeSnapshot> nodes = Collections.emptyList();
    private UiNodeSnapshot selectedNode;
    private float lastTapX = -1f;
    private float lastTapY = -1f;

    public InspectorOverlayView(Context context) {
        super(context);
        init();
    }

    public InspectorOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(dp(1.2f));

        fillPaint.setStyle(Paint.Style.FILL);

        selectedStrokePaint.setStyle(Paint.Style.STROKE);
        selectedStrokePaint.setColor(0xFFFFD54F);
        selectedStrokePaint.setStrokeWidth(dp(2.4f));

        selectedFillPaint.setStyle(Paint.Style.FILL);
        selectedFillPaint.setColor(0x22FFD54F);

        labelFillPaint.setStyle(Paint.Style.FILL);
        labelFillPaint.setColor(0xEE111827);

        labelTextPaint.setColor(Color.WHITE);
        labelTextPaint.setTextSize(dp(11f));

        tapPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        tapPaint.setColor(0xAA4DD0E1);
        tapPaint.setStrokeWidth(dp(1.5f));
    }

    void setSnapshot(UiWindowSnapshot snapshot) {
        this.snapshot = snapshot;
        this.nodes = snapshot == null ? Collections.<UiNodeSnapshot>emptyList()
                : NodeHitTestHelper.flattenVisibleNodes(snapshot.getRoot());
        this.selectedNode = null;
        this.lastTapX = -1f;
        this.lastTapY = -1f;
        invalidate();
    }

    UiNodeSnapshot selectAt(int x, int y) {
        lastTapX = x;
        lastTapY = y;
        if (snapshot == null || snapshot.getRoot() == null) {
            selectedNode = null;
        } else {
            selectedNode = NodeHitTestHelper.findDeepestNodeAt(snapshot.getRoot(), x, y);
        }
        invalidate();
        return selectedNode;
    }

    void clearSelection() {
        selectedNode = null;
        lastTapX = -1f;
        lastTapY = -1f;
        invalidate();
    }

    UiNodeSnapshot getSelectedNode() {
        return selectedNode;
    }

    int getNodeCount() {
        return nodes.size();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (UiNodeSnapshot node : nodes) {
            RectSnapshot rect = node.getScreenBoundsDetail();
            if (rect == null) {
                continue;
            }
            drawNode(canvas, node, rect, node == selectedNode);
        }

        if (selectedNode != null) {
            drawSelectedLabel(canvas, selectedNode);
        }

        if (lastTapX >= 0f && lastTapY >= 0f) {
            float radius = dp(6f);
            canvas.drawCircle(lastTapX, lastTapY, radius, tapPaint);
        }
    }

    private void drawNode(Canvas canvas, UiNodeSnapshot node, RectSnapshot rect, boolean selected) {
        RectF rectF = new RectF(rect.getLeft(), rect.getTop(), rect.getRight(), rect.getBottom());
        int strokeColor = resolveNodeColor(node);
        float corner = dp(5f);

        if (!selected) {
            strokePaint.setColor(applyAlpha(strokeColor, 180));
            fillPaint.setColor(applyAlpha(strokeColor, 30));
            canvas.drawRoundRect(rectF, corner, corner, fillPaint);
            canvas.drawRoundRect(rectF, corner, corner, strokePaint);
            return;
        }

        canvas.drawRoundRect(rectF, corner, corner, selectedFillPaint);
        canvas.drawRoundRect(rectF, corner, corner, selectedStrokePaint);
    }

    private void drawSelectedLabel(Canvas canvas, UiNodeSnapshot node) {
        RectSnapshot rect = node.getScreenBoundsDetail();
        if (rect == null) {
            return;
        }

        String label = InspectorTextFormatter.buildNodeLabel(node);
        float textWidth = labelTextPaint.measureText(label);
        float paddingHorizontal = dp(10f);
        float paddingVertical = dp(7f);
        float labelWidth = textWidth + paddingHorizontal * 2f;
        float labelHeight = labelTextPaint.getTextSize() + paddingVertical * 2f;
        float left = Math.max(dp(8f), Math.min(rect.getLeft(), getWidth() - labelWidth - dp(8f)));
        float top = rect.getTop() - labelHeight - dp(8f);
        if (top < dp(12f)) {
            top = rect.getBottom() + dp(8f);
        }
        if (top + labelHeight > getHeight() - dp(8f)) {
            top = Math.max(dp(8f), getHeight() - labelHeight - dp(8f));
        }

        RectF background = new RectF(left, top, left + labelWidth, top + labelHeight);
        canvas.drawRoundRect(background, dp(12f), dp(12f), labelFillPaint);
        canvas.drawText(label, left + paddingHorizontal, top + paddingVertical + labelTextPaint.getTextSize() * 0.82f, labelTextPaint);
    }

    private int resolveNodeColor(UiNodeSnapshot node) {
        if (node.isEditable()) {
            return 0xFFFFB74D;
        }
        if (node.isScrollable()) {
            return 0xFF64B5F6;
        }
        if (node.isClickable()) {
            return 0xFF4DD0E1;
        }
        if (node.isFocusable()) {
            return 0xFFAED581;
        }
        return 0xFFE6EE9C;
    }

    private int applyAlpha(int color, int alpha) {
        return Color.argb(
                Math.max(0, Math.min(255, alpha)),
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        );
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
