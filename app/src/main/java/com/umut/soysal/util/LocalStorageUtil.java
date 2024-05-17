package com.umut.soysal.util;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import com.umut.soysal.modal.CardDataModel;

public class LocalStorageUtil {
    private static final String LANGUAGES_KEY = "Client-Language";
    private static final String BASE_URL = "baseUrl";
    private static final String CLIENT_ID_KEY = "CIC-Client-Id";
    private static final String CLIENT_NAME_KEY = "CIC-Client-Name";
    private static final String CLIENT_DEVICE_ID = "CIC-Device-Id";
    private static final String TOKEN = "token";
    private static final String TOKEN_ID = "tokenId";
    private static final String USERNAME = "userName";
    private static final String ACCOUNT_ID = "accountId";
    private static final String MANAGER_ID = "managerId";
    private static final String ISSUER = "isSuer";
    private static final String WEBSOCKET = "websocket";
    private static final String CAN = "can";
    private static final String QRCODE = "dataQRScan";
    private static final String URL_WEBSOCKET = "urlWebsocket";
    private static final String VERSION = "version";
    private static final String ISCHANGEURL = "is_change_url";




    // Hàm để ghi giá trị ngôn ngữ vào LocalStorage
    public static void writeBaseUrl(Context context, String baseUrl) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(BASE_URL, baseUrl);
        editor.apply();
    }

    public static void writeLanguages(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LANGUAGES_KEY, language);
        editor.apply();
    }

    public static void writeVersion(Context context, String version) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        if (version != null) {
            editor.putString(VERSION, version);
        } else {
            editor.remove(VERSION);
        }
        editor.apply();
    }
    public static void writeCAN(Context context, String can) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        if (can != null) {
            editor.putString(CAN, can);
        } else {
            editor.remove(CAN);
        }
        editor.apply();
    }

    public static void writeQRCode(Context context, String qrCode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        if (qrCode != null) {
            editor.putString(CAN, qrCode);
        } else {
            editor.remove(CAN);
        }
        editor.apply();
    }

    public static void writeIsChangeUrl(Context context, Boolean isChangeUrl) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        if (isChangeUrl != null) {
            editor.putBoolean(ISCHANGEURL, isChangeUrl);
        } else {
            editor.remove(ISCHANGEURL);
        }
        editor.apply();
    }

    public static void writeToken(Context context, String token) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        if (token != null) {
            editor.putString(TOKEN, token);
        } else {
            editor.remove(TOKEN);
        }
        editor.apply();
    }

    public static void writeClientId(Context context, String clientId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        if (clientId != null) {
            editor.putString(CLIENT_ID_KEY, clientId);
        } else {
            editor.remove(CLIENT_ID_KEY);
        }
        editor.apply();
    }

    public static void writeClientName(Context context, String clientName) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        if (clientName != null) {
            editor.putString(CLIENT_NAME_KEY, clientName);
        } else {
            editor.remove(CLIENT_NAME_KEY);
        }
        editor.apply();
    }

    public static void writeDeviceId(Context context, String deviceId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        if (deviceId != null) {
            editor.putString(CLIENT_DEVICE_ID, deviceId);
        } else {
            editor.remove(CLIENT_DEVICE_ID);
        }
        editor.apply();
    }

    public static void writeWebsocket(Context context, Boolean websocket) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        if (websocket != null) {
            editor.putBoolean(WEBSOCKET, websocket);
        } else {
            editor.remove(WEBSOCKET);
        }
        editor.apply();
    }

    public static void writeUrlWebsocket(Context context, String urlWebsocket) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        if (urlWebsocket != null) {
            editor.putString(URL_WEBSOCKET, urlWebsocket);
        } else {
            editor.remove(URL_WEBSOCKET);
        }
        editor.apply();
    }

    public static void writeIsSuer(Context context, String issuer) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        if (issuer != null) {
            editor.putString(ISSUER, issuer);
        } else {
            editor.remove(ISSUER);
        }
        editor.apply();
    }

    public static void saveProfileAccountId(Context context, String accountId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(ACCOUNT_ID, accountId).apply();
    }

    public static void saveProfileAccountName(Context context, String userName) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(USERNAME, userName).apply();
    }

    public static void saveManagerId(Context context, String managerId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(MANAGER_ID, managerId).apply();
    }

    public static void saveConfigValidation(@NonNull Context context, boolean checkIntegrity, boolean checkLegitimacy, boolean checkFaceCompare, boolean faceLivenessCheck) {
        SharedPreferences preferences = context.getSharedPreferences("config_validation_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("checkIntegrity", checkIntegrity);
        editor.putBoolean("checkLegitimacy", checkLegitimacy);
        editor.putBoolean("checkFaceCompare", checkFaceCompare);
        editor.putBoolean("faceLivenessCheck", faceLivenessCheck);

        editor.apply();
    }

    // Hàm để đọc giá trị client ID từ LocalStorage
    public static String readBaseUrl(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(BASE_URL, null);
    }

    public static boolean readIsChangeUrl(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(ISCHANGEURL, false);
    }

    public static String readVersion(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(VERSION, "");
    }
    public static String readClientId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(CLIENT_ID_KEY, "");
    }

    public static String readIsSuer(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(ISSUER, "");
    }

    public static String readDeviceId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(CLIENT_DEVICE_ID, "");
    }

    public static String readClientName(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(CLIENT_NAME_KEY, "");
    }

    // Hàm để đọc giá trị token từ LocalStorage
    public static String readToken(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(TOKEN, "");
    }

    public static String readTokenId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(TOKEN_ID, "");
    }

    public static String readProfileUsername(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(USERNAME, "");
    }

    public static String readProfileAccountId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(ACCOUNT_ID, "");
    }

    public static String readManagerId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(MANAGER_ID, "");
    }

    public static Boolean readWebsocket(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(WEBSOCKET, false);
    }

    public static String readUrlWebsocket(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(URL_WEBSOCKET, "");
    }

    public static String readCAN(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(CAN, "");
    }

    public static String readQrCode(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(QRCODE, null);
    }


    public static boolean readConfigValidation(@NonNull Context context, String key) {
        SharedPreferences preferences = context.getSharedPreferences("config_validation_preferences", Context.MODE_PRIVATE);
        return preferences.getBoolean(key, false);
    }

    @NonNull
    public static CardDataModel readCardData(@NonNull Context context) {
        SharedPreferences preferences = context.getSharedPreferences("card_data", Context.MODE_PRIVATE);

        CardDataModel cardDataModel = new CardDataModel();
        cardDataModel.setDg1(preferences.getString("dg1", ""));
        cardDataModel.setDg2(preferences.getString("dg2", ""));
        cardDataModel.setDg13(preferences.getString("dg13", ""));
        cardDataModel.setDg14(preferences.getString("dg14", ""));
        cardDataModel.setDg15(preferences.getString("dg15", ""));
        cardDataModel.setSod(preferences.getString("sod", ""));
        cardDataModel.setCardNumber(preferences.getString("card_number", ""));

        return cardDataModel;
    }

    // Hàm xóa dữ liệu xác thực đang lưu ảo
    public static void clearDataAuthen(Context context) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove("dg1Data");
            editor.remove("dg2Data");
            editor.remove("dg13Data");
            editor.remove("dg14Data");
            editor.remove("dg15Data");
            editor.remove("sodData");
            editor.remove("cardNumber");
            editor.apply();

            SharedPreferences sharedPreferencesImage = context.getSharedPreferences("Image", Context.MODE_PRIVATE);
            SharedPreferences.Editor editorImage = sharedPreferencesImage.edit();
            editorImage.remove("base64ImageCap");
            editorImage.apply();

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void clearDataAuthed(Context context) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove("dg1");
            editor.remove("dg2");
            editor.remove("dg13");
            editor.remove("dg14");
            editor.remove("dg15");
            editor.remove("sod");
            editor.remove("can");
            editor.remove("mrz");
            editor.apply();

            SharedPreferences sharedPreferencesImage = context.getSharedPreferences("Image", Context.MODE_PRIVATE);
            SharedPreferences.Editor editorImage = sharedPreferencesImage.edit();
            editorImage.remove("base64ImageCap");
            editorImage.apply();

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void clearData(Context context) {
        try {
            // Lưu trữ giá trị của các trường dữ liệu mà bạn muốn giữ lại
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String baseUrl = readBaseUrl(context);
            String urlWebsocket = readUrlWebsocket(context);
            boolean checkIntegrity = readConfigValidation(context, "checkIntegrity");
            boolean checkLegitimacy = readConfigValidation(context, "checkLegitimacy");
            boolean checkFaceCompare = readConfigValidation(context, "checkFaceCompare");
            boolean faceLivenessCheck = readConfigValidation(context, "faceLivenessCheck");

            // Xóa tất cả dữ liệu từ SharedPreferences
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

            // Khôi phục lại các giá trị của các trường dữ liệu đã lưu trữ
            writeBaseUrl(context, baseUrl);
            writeUrlWebsocket(context, urlWebsocket);
            saveConfigValidation(context, checkIntegrity, checkLegitimacy, checkFaceCompare, faceLivenessCheck);

            // Xóa các trường dữ liệu khác
            SharedPreferences sharedPreferencesImage = context.getSharedPreferences("Image", Context.MODE_PRIVATE);
            SharedPreferences.Editor editorImage = sharedPreferencesImage.edit();
            editorImage.clear();
            editorImage.apply();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static boolean getCheckIntegrity(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("checkIntegrity", false);
    }
    public static boolean getCheckLegitimacy(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("checkLegitimacy", false);
    }
    public static boolean getCheckFaceCompare(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("checkFaceCompare", false);
    }
    public static boolean getCheckFaceLivenessCheck(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("faceLivenessCheck", false);
    }

}
