package com.umut.soysal.api;

import static com.umut.soysal.util.LocalStorageUtil.readBaseUrl;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.umut.soysal.util.LocalStorageUtil;
import com.umut.soysal.util.ToastContainer;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class ApiClient {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    private ApiClient() {
    }

    public static ApiService getApiService(Context context) {
        try {
            String url = readBaseUrl(context);
            if (url == null) {
                ToastContainer toastContainer = new ToastContainer();
                toastContainer.showToast(context, "Chưa cấu hình CIC Server");
                return null;
            }
            HttpUrl baseUrl = HttpUrl.get(url);
            if (retrofit == null) {
                OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

                httpClient.addInterceptor(chain -> {
                    Request original = chain.request();
                    readClientName(context);
                    Request request = original.newBuilder()
                            .header("Content-Type", "application/json")
                            .header("Access-Control-Allow-Origin", "*")
                            .header("Access-Control-Allow-Methods", "*")
                            .header("Access-Control-Allow-Credentials", "true")
                            .header("Client-Language", "vi")
                            .header("Authorization", "Bearer " + readToken(context))
                            .header("CIC-Client-Id", readClientId(context))
                            .header("CIC-Client-Name", "VTCs-33.1")
                            .header("CIC-Device-Id", readDeviceId(context))
                            .method(original.method(), original.body())
                            .build();

                    return chain.proceed(request);
                });

                int timeoutInSeconds = 60;
                httpClient.connectTimeout(timeoutInSeconds, TimeUnit.SECONDS);
                httpClient.readTimeout(timeoutInSeconds, TimeUnit.SECONDS);
                httpClient.writeTimeout(timeoutInSeconds, TimeUnit.SECONDS);

                retrofit = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(JacksonConverterFactory.create(OBJECT_MAPPER))
                        .client(httpClient.build())
                        .build();

            }
            if (!Objects.equals(retrofit.baseUrl(), baseUrl)) {
                retrofit = retrofit.newBuilder().baseUrl(baseUrl).build();
                apiService = retrofit.create(ApiService.class);
            }
            if (apiService == null) {
                apiService = retrofit.create(ApiService.class);
            }
            return apiService;
        } catch (Exception e) {
            ToastContainer toastContainer = new ToastContainer();
            toastContainer.showToast(context, "Chưa cấu hình CIC Server");
            return null;
        }
    }


    // Các hàm helper để đọc thông tin từ LocalStorage
    @Nullable
    private static String readToken(Context context) {
        String token = LocalStorageUtil.readToken(context);
        return (token != null && !token.isEmpty()) ? token : null;
    }

    @NonNull
    private static String readClientId(Context context) {
        String clientId = LocalStorageUtil.readClientId(context);
        return (clientId != null && !clientId.isEmpty()) ? clientId : "";
    }

    @NonNull
    private static String readClientName(Context context) {
        String clientName = LocalStorageUtil.readClientName(context);
        return (clientName != null && !clientName.isEmpty()) ? clientName : "";
    }

    @NonNull
    private static String readDeviceId(Context context) {
        String deviceId = LocalStorageUtil.readDeviceId(context);
        return (deviceId != null && !deviceId.isEmpty()) ? deviceId : "";
    }
}
