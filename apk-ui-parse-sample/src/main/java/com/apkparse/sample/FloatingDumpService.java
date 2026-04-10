package com.apkparse.sample;

import android.animation.ValueAnimator;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowMetrics;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.apkparse.android.facade.UiParse;
import com.apkparse.core.model.DumpOptions;
import com.apkparse.core.model.DumpResult;
import com.apkparse.core.model.UiNodeSnapshot;
import com.apkparse.core.model.UiWindowSnapshot;

import java.io.File;
import java.io.IOException;

public class FloatingDumpService extends Service {
    private static final String TAG = "FloatingDumpService";
    private static volatile FloatingDumpService sharedInstance;

    private Context overlayContext;
    private WindowManager windowManager;
    private View floatingBall;
    private LinearLayout floatingMenu;
    private FrameLayout inspectOverlay;
    private InspectorOverlayView inspectorOverlayView;
    private LinearLayout resultPanel;
    private LinearLayout resultTableContainer;
    private TextView inspectTitleView;
    private TextView inspectHintView;
    private TextView panelTitleView;
    private TextView resultTextView;

    private WindowManager.LayoutParams ballLayoutParams;
    private WindowManager.LayoutParams menuLayoutParams;
    private WindowManager.LayoutParams inspectLayoutParams;
    private WindowManager.LayoutParams resultLayoutParams;

    private String latestDumpJson;
    private String latestPanelText;
    private File latestSavedFile;
    private UiWindowSnapshot latestWindowSnapshot;
    private boolean menuVisible;
    private boolean inspectMode;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static boolean isRunning() {
        return sharedInstance != null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        sharedInstance = this;
        overlayContext = createOverlayContext();
        windowManager = (WindowManager) overlayContext.getSystemService(WINDOW_SERVICE);
        if (windowManager == null) {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }
        showFloatingBall();
        showFloatingMenu();
        showInspectOverlay();
        showResultPanel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand overlay=" + Settings.canDrawOverlays(this));
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "overlay permission is required", Toast.LENGTH_SHORT).show();
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        removeFloatingViews();
        sharedInstance = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showFloatingBall() {
        if (!Settings.canDrawOverlays(this) || floatingBall != null) {
            Log.d(TAG, "showFloatingBall skipped overlay=" + Settings.canDrawOverlays(this) + " ball=" + (floatingBall != null));
            return;
        }
        Log.i(TAG, "showFloatingBall");

        TextView ball = new TextView(overlayContext);
        ball.setText("UI");
        ball.setTextSize(13f);
        ball.setTextColor(Color.WHITE);
        ball.setGravity(Gravity.CENTER);
        int size = dp(58);
        ball.setWidth(size);
        ball.setHeight(size);
        ball.setMinWidth(size);
        ball.setMinHeight(size);
        ball.setBackground(createBallBackground());
        if (Build.VERSION.SDK_INT >= 21) {
            ball.setElevation(dp(8));
        }

        ballLayoutParams = createBaseLayoutParams();
        ballLayoutParams.width = size;
        ballLayoutParams.height = size;
        ballLayoutParams.gravity = Gravity.TOP | Gravity.START;
        ballLayoutParams.x = getScreenWidth() - size - dp(16);
        ballLayoutParams.y = dp(220);

        ball.setOnTouchListener(new FloatingTouchListener());

        floatingBall = ball;
        windowManager.addView(floatingBall, ballLayoutParams);
    }

    private void showFloatingMenu() {
        if (!Settings.canDrawOverlays(this) || floatingMenu != null) {
            Log.d(TAG, "showFloatingMenu skipped overlay=" + Settings.canDrawOverlays(this) + " menu=" + (floatingMenu != null));
            return;
        }
        Log.i(TAG, "showFloatingMenu");

        floatingMenu = new LinearLayout(overlayContext);
        floatingMenu.setOrientation(LinearLayout.VERTICAL);
        floatingMenu.setPadding(dp(12), dp(12), dp(12), dp(12));
        floatingMenu.setBackground(createMenuBackground());
        if (Build.VERSION.SDK_INT >= 21) {
            floatingMenu.setElevation(dp(10));
        }
        floatingMenu.setVisibility(View.GONE);

        TextView title = new TextView(overlayContext);
        title.setText("UI Tool");
        title.setTextColor(Color.WHITE);
        title.setTextSize(13f);
        title.setPadding(0, 0, 0, dp(8));
        floatingMenu.addView(title);

        floatingMenu.addView(createMenuItem("Inspect UI", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterInspectMode();
            }
        }));
        floatingMenu.addView(createMenuItem("Refresh Snapshot", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshCurrentState();
            }
        }));
        floatingMenu.addView(createMenuItem("Parse Current UI", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parseAndShowResult();
            }
        }));
        floatingMenu.addView(createMenuItem("Save JSON", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveLatestDump();
            }
        }));
        floatingMenu.addView(createMenuItem("Share JSON", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareLatestDump();
            }
        }));
        floatingMenu.addView(createMenuItem("Open Result", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLatestResult();
            }
        }));
        floatingMenu.addView(createMenuItem("Close Tool", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        }));

        menuLayoutParams = createBaseLayoutParams();
        menuLayoutParams.width = dp(188);
        menuLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        menuLayoutParams.gravity = Gravity.TOP | Gravity.START;

        windowManager.addView(floatingMenu, menuLayoutParams);
        updateMenuPosition();
    }

    private void showInspectOverlay() {
        if (!Settings.canDrawOverlays(this) || inspectOverlay != null) {
            Log.d(TAG, "showInspectOverlay skipped overlay=" + Settings.canDrawOverlays(this) + " inspect=" + (inspectOverlay != null));
            return;
        }
        Log.i(TAG, "showInspectOverlay");

        inspectOverlay = new FrameLayout(overlayContext);
        inspectOverlay.setBackgroundColor(0x14000000);
        inspectOverlay.setVisibility(View.GONE);

        inspectorOverlayView = new InspectorOverlayView(overlayContext);
        inspectorOverlayView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!inspectMode) {
                    return false;
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    inspectAt((int) event.getRawX(), (int) event.getRawY());
                    return true;
                }
                return true;
            }
        });
        inspectOverlay.addView(inspectorOverlayView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        LinearLayout topBar = new LinearLayout(overlayContext);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setPadding(dp(14), dp(12), dp(14), dp(12));
        topBar.setBackground(createMenuBackground());

        inspectTitleView = new TextView(overlayContext);
        inspectTitleView.setText("Inspector Mode");
        inspectTitleView.setTextColor(Color.WHITE);
        inspectTitleView.setTextSize(14f);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        topBar.addView(inspectTitleView, titleParams);
        topBar.addView(createHeaderAction("REFRESH", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshInspectSnapshot();
            }
        }));
        topBar.addView(createHeaderAction("EXIT", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitInspectMode();
            }
        }));

        FrameLayout.LayoutParams topParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        topParams.gravity = Gravity.TOP;
        topParams.leftMargin = dp(12);
        topParams.topMargin = dp(28);
        topParams.rightMargin = dp(12);
        inspectOverlay.addView(topBar, topParams);

        inspectHintView = new TextView(overlayContext);
        inspectHintView.setText("Tap any box to inspect details");
        inspectHintView.setTextColor(Color.WHITE);
        inspectHintView.setTextSize(12f);
        inspectHintView.setPadding(dp(12), dp(9), dp(12), dp(9));
        inspectHintView.setBackground(createPanelBackground());
        FrameLayout.LayoutParams hintParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        hintParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        hintParams.bottomMargin = dp(26);
        inspectOverlay.addView(inspectHintView, hintParams);

        inspectLayoutParams = createBaseLayoutParams();
        inspectLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        inspectLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        inspectLayoutParams.gravity = Gravity.TOP | Gravity.START;

        windowManager.addView(inspectOverlay, inspectLayoutParams);
    }

    private void showResultPanel() {
        if (!Settings.canDrawOverlays(this) || resultPanel != null) {
            Log.d(TAG, "showResultPanel skipped overlay=" + Settings.canDrawOverlays(this) + " panel=" + (resultPanel != null));
            return;
        }
        Log.i(TAG, "showResultPanel");

        resultPanel = new LinearLayout(overlayContext);
        resultPanel.setOrientation(LinearLayout.VERTICAL);
        resultPanel.setPadding(dp(14), dp(14), dp(14), dp(14));
        resultPanel.setBackground(createPanelBackground());
        if (Build.VERSION.SDK_INT >= 21) {
            resultPanel.setElevation(dp(12));
        }
        resultPanel.setVisibility(View.GONE);

        LinearLayout header = new LinearLayout(overlayContext);
        header.setOrientation(LinearLayout.HORIZONTAL);

        panelTitleView = new TextView(this);
        panelTitleView.setText("Inspector");
        panelTitleView.setTextColor(Color.WHITE);
        panelTitleView.setTextSize(15f);
        panelTitleView.setPadding(0, 0, dp(12), dp(8));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        header.addView(panelTitleView, titleParams);
        header.addView(createHeaderAction("REFRESH", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshCurrentState();
            }
        }));
        header.addView(createHeaderAction("COPY", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyCurrentText();
            }
        }));
        header.addView(createHeaderAction("HIDE", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResultPanelVisible(false);
            }
        }));
        resultPanel.addView(header);

        ScrollView scrollView = new ScrollView(overlayContext);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(320)
        );
        LinearLayout contentContainer = new LinearLayout(overlayContext);
        contentContainer.setOrientation(LinearLayout.VERTICAL);

        resultTableContainer = new LinearLayout(overlayContext);
        resultTableContainer.setOrientation(LinearLayout.VERTICAL);
        resultTableContainer.setVisibility(View.GONE);
        contentContainer.addView(resultTableContainer, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        resultTextView = new TextView(overlayContext);
        resultTextView.setTextColor(Color.WHITE);
        resultTextView.setTextSize(12f);
        resultTextView.setMovementMethod(new ScrollingMovementMethod());
        resultTextView.setTextIsSelectable(true);
        resultTextView.setText("Tap the floating ball to enter inspector mode.");
        contentContainer.addView(resultTextView, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));
        scrollView.addView(contentContainer, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));
        resultPanel.addView(scrollView, scrollParams);

        resultLayoutParams = createBaseLayoutParams();
        resultLayoutParams.width = Math.min(getScreenWidth() - dp(20), dp(420));
        resultLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        resultLayoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        resultLayoutParams.y = dp(20);

        windowManager.addView(resultPanel, resultLayoutParams);
    }

    private TextView createMenuItem(String text, View.OnClickListener listener) {
        TextView item = new TextView(overlayContext);
        item.setText(text);
        item.setTextSize(14f);
        item.setTextColor(Color.WHITE);
        item.setPadding(dp(12), dp(10), dp(12), dp(10));
        item.setBackground(createMenuItemBackground());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(6);
        item.setLayoutParams(params);
        item.setOnClickListener(listener);
        return item;
    }

    private TextView createHeaderAction(String label, View.OnClickListener listener) {
        TextView action = new TextView(overlayContext);
        action.setText(label);
        action.setTextColor(0xFFB3E5FC);
        action.setTextSize(12f);
        action.setPadding(dp(8), dp(4), dp(8), dp(4));
        action.setOnClickListener(listener);
        return action;
    }

    private void toggleMenu() {
        setMenuVisible(!menuVisible);
    }

    private void setMenuVisible(boolean visible) {
        menuVisible = visible;
        if (floatingMenu == null) {
            return;
        }
        updateMenuPosition();
        floatingMenu.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible && Build.VERSION.SDK_INT >= 21) {
            floatingMenu.setAlpha(0f);
            floatingMenu.animate().alpha(1f).setDuration(140L).start();
        }
    }

    private void setInspectOverlayVisible(boolean visible) {
        if (inspectOverlay == null) {
            return;
        }
        inspectOverlay.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setResultPanelVisible(boolean visible) {
        if (resultPanel == null) {
            return;
        }
        resultPanel.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible && Build.VERSION.SDK_INT >= 21) {
            resultPanel.setAlpha(0f);
            resultPanel.animate().alpha(1f).setDuration(140L).start();
        }
    }

    private void updateMenuPosition() {
        if (menuLayoutParams == null || ballLayoutParams == null) {
            return;
        }
        int margin = dp(12);
        int menuWidth = dp(188);
        int ballSize = dp(58);
        boolean placeLeft = ballLayoutParams.x > getScreenWidth() / 2;
        menuLayoutParams.x = placeLeft
                ? Math.max(dp(8), ballLayoutParams.x - menuWidth - margin)
                : Math.min(getScreenWidth() - menuWidth - dp(8), ballLayoutParams.x + ballSize + margin);
        menuLayoutParams.y = Math.max(dp(80), ballLayoutParams.y - dp(10));
        if (floatingMenu != null && floatingMenu.getWindowToken() != null) {
            windowManager.updateViewLayout(floatingMenu, menuLayoutParams);
        }
    }

    private void enterInspectMode() {
        Log.i(TAG, "enterInspectMode");
        setMenuVisible(false);
        updateResultPanel("Inspector", "Capturing current UI snapshot...");
        setResultPanelVisible(true);
        setInspectOverlayVisible(true);
        updateBallText("...");
        if (!refreshInspectSnapshot()) {
            Log.w(TAG, "enterInspectMode refresh failed");
            updateBallText("UI");
            return;
        }
        inspectMode = true;
        setInspectOverlayVisible(true);
        setResultPanelVisible(true);
        updateBallText("UI");
        Toast.makeText(this, "inspector snapshot ready", Toast.LENGTH_SHORT).show();
    }

    private void exitInspectMode() {
        Log.i(TAG, "exitInspectMode");
        inspectMode = false;
        if (inspectorOverlayView != null) {
            inspectorOverlayView.clearSelection();
        }
        setInspectOverlayVisible(false);
        updateInspectChrome(null);
        updateBallText("UI");
    }

    private boolean refreshInspectSnapshot() {
        Log.i(TAG, "refreshInspectSnapshot");
        DumpResult result = dumpCurrentWindow();
        if (!result.isSuccess() || result.getSnapshot() == null || result.getSnapshot().getRoot() == null) {
            Log.w(TAG, "refreshInspectSnapshot failed code=" + result.getErrorCode() + " message=" + result.getErrorMessage());
            latestDumpJson = null;
            latestSavedFile = null;
            latestWindowSnapshot = null;
            inspectMode = false;
            setInspectOverlayVisible(false);
            updateResultPanel(
                    "Inspect Failed",
                    "errorCode: " + result.getErrorCode()
                            + "\nmessage: " + result.getErrorMessage()
                            + "\n\nCheck whether the accessibility service is enabled."
            );
            setResultPanelVisible(true);
            return false;
        }

        latestDumpJson = result.getJson();
        latestSavedFile = null;
        latestWindowSnapshot = result.getSnapshot();
        Log.i(
                TAG,
                "refreshInspectSnapshot success jsonLength=" + (latestDumpJson == null ? 0 : latestDumpJson.length())
                        + " package=" + safeMetaField(latestWindowSnapshot, true)
                        + " activity=" + safeMetaField(latestWindowSnapshot, false)
        );
        if (inspectorOverlayView != null) {
            inspectorOverlayView.setSnapshot(latestWindowSnapshot);
        }
        updateInspectChrome(latestWindowSnapshot);
        updateSummaryPanel(latestWindowSnapshot, inspectorOverlayView == null ? 0 : inspectorOverlayView.getNodeCount());
        return true;
    }

    private void refreshCurrentState() {
        setMenuVisible(false);
        if (inspectMode) {
            refreshInspectSnapshot();
            setInspectOverlayVisible(true);
            setResultPanelVisible(true);
            return;
        }
        parseAndShowResult();
    }

    private void parseAndShowResult() {
        Log.i(TAG, "parseAndShowResult");
        DumpResult result = dumpCurrentWindow();
        if (!result.isSuccess()) {
            Log.w(TAG, "parseAndShowResult failed code=" + result.getErrorCode() + " message=" + result.getErrorMessage());
            latestDumpJson = null;
            latestSavedFile = null;
            latestWindowSnapshot = null;
            setMenuVisible(false);
            updateResultPanel(
                    "Parse Failed",
                    "errorCode: " + result.getErrorCode()
                            + "\nmessage: " + result.getErrorMessage()
                            + "\n\nCheck whether the accessibility service is enabled."
            );
            setResultPanelVisible(true);
            return;
        }

        latestDumpJson = result.getJson();
        latestSavedFile = null;
        latestWindowSnapshot = result.getSnapshot();
        Log.i(TAG, "parseAndShowResult success jsonLength=" + (latestDumpJson == null ? 0 : latestDumpJson.length()));
        setMenuVisible(false);
        updateResultPanel("Window JSON", latestDumpJson);
        setResultPanelVisible(true);
    }

    private void inspectAt(int x, int y) {
        Log.i(TAG, "inspectAt x=" + x + " y=" + y);
        if (latestWindowSnapshot == null || latestWindowSnapshot.getRoot() == null || inspectorOverlayView == null) {
            if (!refreshInspectSnapshot()) {
                Log.w(TAG, "inspectAt refresh on demand failed");
                return;
            }
        }

        UiNodeSnapshot selectedNode = inspectorOverlayView.selectAt(x, y);
        if (selectedNode == null) {
            Log.i(TAG, "inspectAt no node matched");
            updateResultPanel("Inspector", "No node matched tap point: " + x + "," + y);
            setResultPanelVisible(true);
            return;
        }
        Log.i(TAG, "inspectAt selected node=" + selectedNode.getNodeKey() + " class=" + selectedNode.getClassName());

        updateSelectionPanel(latestWindowSnapshot, selectedNode, x, y);
        setResultPanelVisible(true);
    }

    private void saveLatestDump() {
        if (!ensureLatestDump()) {
            return;
        }

        try {
            latestSavedFile = FileExportHelper.saveJsonToFile(this, latestDumpJson);
            Toast.makeText(this, "saved: " + latestSavedFile.getName(), Toast.LENGTH_SHORT).show();
            setMenuVisible(false);
        } catch (IOException exception) {
            Toast.makeText(this, "save failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareLatestDump() {
        try {
            ensureLatestFile();
            setMenuVisible(false);
            Intent chooser = Intent.createChooser(
                    FileExportHelper.createShareIntent(this, latestSavedFile),
                    "Share JSON"
            );
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(chooser);
        } catch (IOException exception) {
            Toast.makeText(this, "share failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openLatestResult() {
        try {
            ensureLatestFile();
            setMenuVisible(false);
            Intent intent = new Intent(this, MainActivity.class);
            intent.setAction(MainActivity.ACTION_VIEW_RESULT);
            intent.putExtra(MainActivity.EXTRA_RESULT_FILE_PATH, latestSavedFile.getAbsolutePath());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } catch (IOException exception) {
            Toast.makeText(this, "open failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean ensureLatestDump() {
        if (latestDumpJson != null && latestDumpJson.length() > 0) {
            return true;
        }

        DumpResult result = dumpCurrentWindow();
        if (!result.isSuccess()) {
            Toast.makeText(this, "dump failed: " + result.getErrorCode(), Toast.LENGTH_SHORT).show();
            return false;
        }

        latestDumpJson = result.getJson();
        latestSavedFile = null;
        latestWindowSnapshot = result.getSnapshot();
        return true;
    }

    private void ensureLatestFile() throws IOException {
        if (latestSavedFile != null && latestSavedFile.exists()) {
            return;
        }

        if (!ensureLatestDump()) {
            throw new IOException("dump failed");
        }

        latestSavedFile = FileExportHelper.saveJsonToFile(this, latestDumpJson);
    }

    private DumpResult dumpCurrentWindow() {
        return UiParse.dumpTopWindow(new DumpOptions.Builder().prettyJson(true).build());
    }

    private void updateResultPanel(String title, String text) {
        if (panelTitleView != null) {
            panelTitleView.setText(title);
        }
        latestPanelText = text;
        if (resultTableContainer != null) {
            resultTableContainer.removeAllViews();
            resultTableContainer.setVisibility(View.GONE);
        }
        if (resultTextView != null) {
            resultTextView.setVisibility(View.VISIBLE);
            resultTextView.setText(text);
        }
    }

    private void updateSummaryPanel(UiWindowSnapshot windowSnapshot, int nodeCount) {
        if (panelTitleView != null) {
            panelTitleView.setText("Inspector");
        }
        latestPanelText = InspectorTextFormatter.formatOverlaySummary(windowSnapshot, nodeCount);
        if (resultTextView != null) {
            resultTextView.setVisibility(View.GONE);
        }
        if (resultTableContainer != null) {
            resultTableContainer.setVisibility(View.VISIBLE);
            InspectorPanelRenderer.bindSummary(overlayContext, resultTableContainer, windowSnapshot, nodeCount);
        }
    }

    private void updateSelectionPanel(UiWindowSnapshot windowSnapshot, UiNodeSnapshot nodeSnapshot, int x, int y) {
        if (panelTitleView != null) {
            panelTitleView.setText("Selected Node");
        }
        latestPanelText = InspectorTextFormatter.formatSelection(windowSnapshot, nodeSnapshot, x, y);
        if (resultTextView != null) {
            resultTextView.setVisibility(View.GONE);
        }
        if (resultTableContainer != null) {
            resultTableContainer.setVisibility(View.VISIBLE);
            InspectorPanelRenderer.bindSelection(overlayContext, resultTableContainer, windowSnapshot, nodeSnapshot, x, y);
        }
    }

    private void updateBallText(String text) {
        if (floatingBall instanceof TextView) {
            ((TextView) floatingBall).setText(text);
        }
    }

    private void copyCurrentText() {
        if (latestPanelText == null || latestPanelText.length() == 0) {
            return;
        }
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager == null) {
            Toast.makeText(this, "clipboard unavailable", Toast.LENGTH_SHORT).show();
            return;
        }
        clipboardManager.setPrimaryClip(ClipData.newPlainText("ui-inspector", latestPanelText));
        Toast.makeText(this, "copied", Toast.LENGTH_SHORT).show();
    }

    private void updateInspectChrome(UiWindowSnapshot windowSnapshot) {
        if (inspectTitleView == null || inspectHintView == null) {
            return;
        }

        if (windowSnapshot == null || windowSnapshot.getMeta() == null) {
            inspectTitleView.setText("Inspector Mode");
            inspectHintView.setText("Tap any box to inspect details");
            return;
        }

        String packageName = safeMetaField(windowSnapshot, true);
        String activityName = shortActivityName(safeMetaField(windowSnapshot, false));
        inspectTitleView.setText("Inspecting " + shortPackageName(packageName));
        inspectHintView.setText("Tap a highlight to inspect details in " + activityName);
    }

    private WindowManager.LayoutParams createBaseLayoutParams() {
        int overlayType = Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
    }

    private GradientDrawable createBallBackground() {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF0B5FD7, 0xFF0F8B8D}
        );
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setStroke(dp(2), 0x66FFFFFF);
        return drawable;
    }

    private GradientDrawable createMenuBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(0xE61A2233);
        drawable.setCornerRadius(dp(18));
        drawable.setStroke(dp(1), 0x335ED3F3);
        return drawable;
    }

    private GradientDrawable createMenuItemBackground() {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF1565C0, 0xFF00897B}
        );
        drawable.setCornerRadius(dp(14));
        return drawable;
    }

    private GradientDrawable createPanelBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(0xE6191F2B);
        drawable.setCornerRadius(dp(20));
        drawable.setStroke(dp(1), 0x4466D9EF);
        return drawable;
    }

    private void removeFloatingViews() {
        Log.i(TAG, "removeFloatingViews");
        if (floatingMenu != null && windowManager != null) {
            windowManager.removeView(floatingMenu);
        }
        if (inspectOverlay != null && windowManager != null) {
            windowManager.removeView(inspectOverlay);
        }
        if (resultPanel != null && windowManager != null) {
            windowManager.removeView(resultPanel);
        }
        if (floatingBall != null && windowManager != null) {
            windowManager.removeView(floatingBall);
        }
        floatingMenu = null;
        inspectOverlay = null;
        inspectorOverlayView = null;
        resultPanel = null;
        resultTableContainer = null;
        inspectTitleView = null;
        inspectHintView = null;
        panelTitleView = null;
        resultTextView = null;
        floatingBall = null;
    }

    private void snapToEdge() {
        if (floatingBall == null || ballLayoutParams == null) {
            return;
        }
        int screenWidth = getScreenWidth();
        int ballSize = dp(58);
        int targetX = ballLayoutParams.x < (screenWidth - ballSize) / 2 ? dp(8) : screenWidth - ballSize - dp(8);
        ValueAnimator animator = ValueAnimator.ofInt(ballLayoutParams.x, targetX);
        animator.setInterpolator(new OvershootInterpolator(0.9f));
        animator.setDuration(180L);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ballLayoutParams.x = (Integer) animation.getAnimatedValue();
                windowManager.updateViewLayout(floatingBall, ballLayoutParams);
                updateMenuPosition();
            }
        });
        animator.start();
    }

    private int getScreenWidth() {
        if (windowManager == null) {
            DisplayMetrics metrics = getUiResources().getDisplayMetrics();
            return metrics.widthPixels;
        }
        if (Build.VERSION.SDK_INT >= 30) {
            WindowMetrics metrics = windowManager.getCurrentWindowMetrics();
            return metrics.getBounds().width();
        }
        Point point = new Point();
        windowManager.getDefaultDisplay().getRealSize(point);
        return point.x;
    }

    private int dp(int value) {
        float density = getUiResources().getDisplayMetrics().density;
        return (int) (value * density);
    }

    private android.content.res.Resources getUiResources() {
        return overlayContext == null ? getResources() : overlayContext.getResources();
    }

    private Context createOverlayContext() {
        if (Build.VERSION.SDK_INT >= 30) {
            DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
            if (displayManager != null) {
                android.view.Display display = displayManager.getDisplay(android.view.Display.DEFAULT_DISPLAY);
                if (display != null) {
                    return getApplicationContext().createWindowContext(display, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, null);
                }
            }
        }

        WindowManager serviceWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (serviceWindowManager != null && Build.VERSION.SDK_INT >= 17) {
            return createDisplayContext(serviceWindowManager.getDefaultDisplay());
        }
        return this;
    }

    private String safeMetaField(UiWindowSnapshot windowSnapshot, boolean packageField) {
        if (windowSnapshot == null || windowSnapshot.getMeta() == null) {
            return "-";
        }
        String value = packageField ? windowSnapshot.getMeta().getPackageName() : windowSnapshot.getMeta().getActivityName();
        return value == null || value.length() == 0 ? "-" : value;
    }

    private String shortPackageName(String packageName) {
        if (packageName == null || packageName.length() == 0 || "-".equals(packageName)) {
            return "Unknown App";
        }
        int lastIndex = packageName.lastIndexOf('.');
        if (lastIndex < 0 || lastIndex >= packageName.length() - 1) {
            return packageName;
        }
        return packageName.substring(lastIndex + 1);
    }

    private String shortActivityName(String activityName) {
        if (activityName == null || activityName.length() == 0 || "-".equals(activityName)) {
            return "current screen";
        }
        int lastIndex = activityName.lastIndexOf('.');
        if (lastIndex < 0 || lastIndex >= activityName.length() - 1) {
            return activityName;
        }
        return activityName.substring(lastIndex + 1);
    }

    private final class FloatingTouchListener implements View.OnTouchListener {
        private final int touchSlop = ViewConfiguration.get(FloatingDumpService.this).getScaledTouchSlop();
        private final int longPressTimeout = ViewConfiguration.getLongPressTimeout();
        private int initialX;
        private int initialY;
        private float initialTouchX;
        private float initialTouchY;
        private boolean moved;
        private boolean longPressed;
        private final Runnable longPressRunnable = new Runnable() {
            @Override
            public void run() {
                if (!moved) {
                    Log.i(TAG, "floatingBall longPress");
                    longPressed = true;
                    toggleMenu();
                    updateBallText("UI");
                    Toast.makeText(FloatingDumpService.this, "tool menu", Toast.LENGTH_SHORT).show();
                }
            }
        };

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "floatingBall ACTION_DOWN");
                    initialX = ballLayoutParams.x;
                    initialY = ballLayoutParams.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    moved = false;
                    longPressed = false;
                    updateBallText("GO");
                    mainHandler.postDelayed(longPressRunnable, longPressTimeout);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int deltaX = (int) (event.getRawX() - initialTouchX);
                    int deltaY = (int) (event.getRawY() - initialTouchY);
                    if (Math.abs(deltaX) > touchSlop || Math.abs(deltaY) > touchSlop) {
                        moved = true;
                        Log.d(TAG, "floatingBall ACTION_MOVE moved deltaX=" + deltaX + " deltaY=" + deltaY);
                        mainHandler.removeCallbacks(longPressRunnable);
                    }
                    if (moved) {
                        ballLayoutParams.x = initialX + deltaX;
                        ballLayoutParams.y = Math.max(dp(72), initialY + deltaY);
                        windowManager.updateViewLayout(floatingBall, ballLayoutParams);
                        updateMenuPosition();
                        setMenuVisible(false);
                    }
                    return moved;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "floatingBall ACTION_UP moved=" + moved + " longPressed=" + longPressed);
                    mainHandler.removeCallbacks(longPressRunnable);
                    if (moved) {
                        updateBallText("UI");
                        snapToEdge();
                        return true;
                    }
                    if (!longPressed) {
                        Log.i(TAG, "floatingBall click -> enterInspectMode");
                        enterInspectMode();
                    }
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    Log.d(TAG, "floatingBall ACTION_CANCEL");
                    mainHandler.removeCallbacks(longPressRunnable);
                    updateBallText("UI");
                    return true;
                default:
                    return false;
            }
        }
    }
}
