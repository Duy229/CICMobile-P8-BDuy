package com.umut.soysal.util;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class ConvertUtil {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

    public static <T> T fromJson(String json, TypeReference<T> clazz, T defaultValue) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String convertBitmapToBase64(@NonNull Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
    }

    public static <T> T fromJson(String json, Class<T> clazz, T defaultValue) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String toJson(Object json) {
        return toJson(json, null);
    }

    public static String toJson(Object json, String defaultValue) {
        try {
            return OBJECT_MAPPER.writeValueAsString(json);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static byte[] decodeBase64(String s) {
        return decodeBase64(s, null);
    }

    public static byte[] decodeBase64(String s, byte[] defaultValue) {
        try {
            byte[] decoded = Base64.getDecoder().decode(s);
            return decoded != null ? decoded : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
