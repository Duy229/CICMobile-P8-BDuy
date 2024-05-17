package com.umut.soysal.util;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.umut.soysal.api.ApiService;
import com.umut.soysal.modal.AuthClientModal;
import com.umut.soysal.ui.LoginActivity;
import com.umut.soysal.ui.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerformLogout {

    private final ApiService apiService;
    private final Context context;

    public PerformLogout(Context context, ApiService apiService) {
        this.context = context;
        this.apiService = apiService;
    }

    public void performLogout() {
        Call<AuthClientModal.LogoutResponse> logoutResponseCall = apiService.logout();
        logoutResponseCall.enqueue(new Callback<AuthClientModal.LogoutResponse>() {

            @Override
            public void onResponse(@NonNull Call<AuthClientModal.LogoutResponse> call, @NonNull Response<AuthClientModal.LogoutResponse> response) {
                if(response.body() != null &&  response.body().getStatus() == 200) {
                    LocalStorageUtil.writeToken(context, null);
                    LocalStorageUtil.clearData(context);
                    redirectToLoginActivity();
                } else {
                    Toast toast = Toast.makeText(context, "Đăng xuất thất bại.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 10);
                    toast.show();
                }

            }

            @Override
            public void onFailure(@NonNull Call<AuthClientModal.LogoutResponse> call, @NonNull Throwable t) {
                Toast toast = Toast.makeText(context, "Lỗi kết nối. Xin vui lòng kiểm tra kết nối Internet của bạn.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 10);
                toast.show();
            }
        });
    }

    private void redirectToLoginActivity() {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        if (context instanceof MainActivity) {
            ((MainActivity) context).finish();
        }
    }
}
