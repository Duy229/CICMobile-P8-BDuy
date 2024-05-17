package com.umut.soysal.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileLogger {
    private static final String TAG = "FileLogger";
    private static final String LOG_FILE_NAME = "app_log.txt";
    public static void logToFile(Context context, String message) {
        try {
            FileOutputStream fos = context.openFileOutput(LOG_FILE_NAME, Context.MODE_APPEND);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.append(message);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "Error writing to log file: " + e.getMessage());
        }
    }
}

