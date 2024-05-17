package com.umut.soysal.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastContainer {
    public void showToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}
