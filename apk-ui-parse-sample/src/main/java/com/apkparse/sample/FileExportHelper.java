package com.apkparse.sample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

final class FileExportHelper {
    private FileExportHelper() {
    }

    static File saveJsonToFile(Context context, String json) throws IOException {
        File baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (baseDir == null) {
            baseDir = new File(context.getFilesDir(), "exports");
        }

        File exportDir = new File(baseDir, "ui-dumps");
        if (!exportDir.exists() && !exportDir.mkdirs()) {
            throw new IOException("cannot create export directory");
        }

        File outputFile = new File(exportDir, "ui-dump-" + System.currentTimeMillis() + ".json");
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        try {
            writer.write(json);
        } finally {
            writer.close();
        }
        return outputFile;
    }

    static Uri getShareUri(Context context, File file) {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }

    static Intent createShareIntent(Context context, File file) {
        Uri uri = getShareUri(context, file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, file.getName());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    static String readFile(File file) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
        );
        try {
            char[] buffer = new char[2048];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, count);
            }
        } finally {
            reader.close();
        }
        return builder.toString();
    }
}
