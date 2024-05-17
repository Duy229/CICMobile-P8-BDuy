package com.umut.soysal.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;

public class ContainerUtil {
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 4, REQUEST_CODE_CAMERA_PERMISSION = 5;
    @NonNull
    public static String removeBase64Prefix(@NonNull String base64String) {
        if (base64String.startsWith("data:image")) {
            int commaIndex = base64String.indexOf(",") + 1;
            return base64String.substring(commaIndex);
        } else {
            return base64String;
        }
    }

    @NonNull
    public static String getLastSixDigits(@NonNull String input) {
        if (input.length() >= 6) {
            return input.substring(input.length() - 6);
        } else {
            return "";
        }
    }
    @Nullable
    public static Bitmap loadBitmapFromStorage(String fileName) {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), fileName);

            if (file.exists()) {
                return BitmapFactory.decodeFile(file.getAbsolutePath());
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void save_image(Bitmap bitmap, String fileName) {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), fileName);

            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFaceImage(String fileName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName);
        if (file.exists()) {
            if (file.delete()) {
                Log.d("DeleteImage", "Deleted passport image successfully");
            } else {
                Log.d("DeleteImage", "Failed to delete passport image");
            }
        } else {
            Log.d("DeleteImage", "Passport image does not exist");
        }
    }
}
