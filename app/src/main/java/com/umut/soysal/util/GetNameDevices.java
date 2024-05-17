package com.umut.soysal.util;

import android.os.Build;
import android.support.v4.app.ActivityCompat;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class GetNameDevices extends ActivityCompat {
    public static final String clientModel = Build.MODEL + "_" + Build.DEVICE + "_" + Build.ID;

    public static String getClientNameDefault() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;

        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    public static String getSeriNumber() {
        String serial = getCicDeviceSerial();

        if (serial != null) {
            return serial;
        } else {
            return "Không lấy được SeriNumber thiết bị";
        }
    }

    public static String getCicDeviceSerial() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception ignored) {
        }
        return serial;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        char firstChar = s.charAt(0);
        if (Character.isUpperCase(firstChar)) {
            return s;
        } else {
            return Character.toUpperCase(firstChar) + s.substring(1);
        }
    }

    public static String decryptMessage(byte[] encryptedMessage, String password) throws Exception {
        Cipher cipherDecrypt = Cipher.getInstance("AES/ECB/PKCS5Padding");// CIPHER_PASSWORD_TRANSFORMATION = "AES/ECB/PKCS5Padding"
        SecretKeySpec keySpec = new SecretKeySpec(generateHash("SHA256", password.getBytes()), "AES");
        cipherDecrypt.init(Cipher.DECRYPT_MODE, keySpec);// DECRYPT_MODE = 2
        return new String(cipherDecrypt.doFinal(encryptedMessage), StandardCharsets.UTF_8);
    }

    public static byte[] generateHash(String algorithm, byte[] message) {
        try {
            return MessageDigest.getInstance(algorithm).digest(message);
        } catch (Exception e) {
            return null;
        }
    }
}
