package com.apkparse.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.apkparse.android.facade.UiParse;
import com.apkparse.core.model.DumpOptions;
import com.apkparse.core.model.DumpResult;

import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity {
    public static final String ACTION_VIEW_RESULT = "com.apkparse.sample.action.VIEW_RESULT";
    public static final String EXTRA_RESULT_FILE_PATH = "result_file_path";

    private TextView resultView;
    private String latestDumpJson;
    private File latestSavedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildContentView());
        handleIntent(getIntent());
        refreshStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private View buildContentView() {
        int padding = dp(16);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(padding, padding, padding, padding);

        TextView titleView = new TextView(this);
        titleView.setText("APK UI Parse Sample");
        titleView.setTextSize(20f);
        root.addView(titleView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        final TextView statusView = new TextView(this);
        statusView.setTag("statusView");
        root.addView(statusView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button settingsButton = new Button(this);
        settingsButton.setText("Open Accessibility Settings");
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });
        root.addView(settingsButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button overlayPermissionButton = new Button(this);
        overlayPermissionButton.setText("Open Overlay Permission");
        overlayPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(buildOverlaySettingsIntent());
            }
        });
        root.addView(overlayPermissionButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button startFloatingButton = new Button(this);
        startFloatingButton.setText("Start Floating Button");
        startFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Settings.canDrawOverlays(MainActivity.this)) {
                    showToast("grant overlay permission first");
                    startActivity(buildOverlaySettingsIntent());
                    return;
                }
                startService(new Intent(MainActivity.this, FloatingDumpService.class));
                refreshStatus();
            }
        });
        root.addView(startFloatingButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button homeInspectButton = new Button(this);
        homeInspectButton.setText("Go Home For Cross-App Inspect");
        homeInspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Settings.canDrawOverlays(MainActivity.this)) {
                    showToast("grant overlay permission first");
                    startActivity(buildOverlaySettingsIntent());
                    return;
                }
                startService(new Intent(MainActivity.this, FloatingDumpService.class));
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);
            }
        });
        root.addView(homeInspectButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button stopFloatingButton = new Button(this);
        stopFloatingButton.setText("Stop Floating Button");
        stopFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, FloatingDumpService.class));
                refreshStatus();
            }
        });
        root.addView(stopFloatingButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button dumpButton = new Button(this);
        dumpButton.setText("Dump Top Window JSON");
        dumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshStatus();
                DumpResult result = UiParse.dumpTopWindow(new DumpOptions.Builder().prettyJson(true).build());
                if (result.isSuccess()) {
                    latestDumpJson = result.getJson();
                    latestSavedFile = null;
                    resultView.setText(latestDumpJson);
                } else {
                    latestDumpJson = null;
                    latestSavedFile = null;
                    resultView.setText("dump failed: " + result.getErrorCode() + "\n" + result.getErrorMessage());
                }
            }
        });
        root.addView(dumpButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button saveButton = new Button(this);
        saveButton.setText("Save JSON File");
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ensureDumpAvailable()) {
                    return;
                }

                try {
                    latestSavedFile = saveJsonToFile(latestDumpJson);
                    resultView.setText(latestDumpJson + "\n\nsaved file:\n" + latestSavedFile.getAbsolutePath());
                    showToast("saved: " + latestSavedFile.getName());
                } catch (IOException exception) {
                    showToast("save failed: " + exception.getMessage());
                }
            }
        });
        root.addView(saveButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button shareButton = new Button(this);
        shareButton.setText("Share JSON File");
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ensureDumpAvailable()) {
                    return;
                }

                try {
                    if (latestSavedFile == null || !latestSavedFile.exists()) {
                        latestSavedFile = saveJsonToFile(latestDumpJson);
                    }
                    shareFile(latestSavedFile);
                } catch (IOException exception) {
                    showToast("share failed: " + exception.getMessage());
                }
            }
        });
        root.addView(shareButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        ScrollView scrollView = new ScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
        );
        scrollParams.weight = 1f;

        resultView = new TextView(this);
        resultView.setMovementMethod(new ScrollingMovementMethod());
        resultView.setTextIsSelectable(true);
        resultView.setText(
                "Enable accessibility and overlay permission, then start the floating tool.\n\n"
                        + "For cross-app inspect: start the floating tool, switch to another app, and tap the floating ball."
        );
        scrollView.addView(resultView, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        root.addView(scrollView, scrollParams);
        return root;
    }

    private void refreshStatus() {
        TextView statusView = (TextView) findViewById(android.R.id.content).findViewWithTag("statusView");
        if (statusView != null) {
            statusView.setText(
                    "Accessibility: " + UiParse.isServiceConnected()
                            + "\nOverlay permission: " + Settings.canDrawOverlays(this)
                            + "\nFloating tool: " + FloatingDumpService.isRunning()
            );
        }
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (value * density);
    }

    private boolean ensureDumpAvailable() {
        if (latestDumpJson != null && latestDumpJson.length() > 0) {
            return true;
        }
        showToast("please dump json first");
        return false;
    }

    private File saveJsonToFile(String json) throws IOException {
        return FileExportHelper.saveJsonToFile(this, json);
    }

    private void shareFile(File file) {
        startActivity(Intent.createChooser(FileExportHelper.createShareIntent(this, file), "Share JSON"));
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private Intent buildOverlaySettingsIntent() {
        return new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:" + getPackageName())
        );
    }

    private void handleIntent(Intent intent) {
        if (intent == null || !ACTION_VIEW_RESULT.equals(intent.getAction())) {
            return;
        }

        String path = intent.getStringExtra(EXTRA_RESULT_FILE_PATH);
        if (path == null || path.length() == 0) {
            return;
        }

        File file = new File(path);
        if (!file.exists()) {
            resultView.setText("result file not found:\n" + path);
            return;
        }

        latestSavedFile = file;
        try {
            latestDumpJson = FileExportHelper.readFile(file);
            resultView.setText(latestDumpJson + "\n\nopened file:\n" + file.getAbsolutePath());
        } catch (IOException exception) {
            resultView.setText("open failed: " + exception.getMessage());
        }
    }
}
