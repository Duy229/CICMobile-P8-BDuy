package com.umut.soysal.ui;

import static com.umut.soysal.util.GetNameDevices.getClientNameDefault;
import static com.umut.soysal.util.LocalStorageUtil.readBaseUrl;
import static com.umut.soysal.util.LocalStorageUtil.readUrlWebsocket;
import static com.umut.soysal.util.LocalStorageUtil.readWebsocket;
import static com.umut.soysal.util.LocalStorageUtil.writeBaseUrl;
import static com.umut.soysal.util.LocalStorageUtil.writeClientId;
import static com.umut.soysal.util.LocalStorageUtil.writeClientName;
import static com.umut.soysal.util.LocalStorageUtil.writeIsSuer;
import static com.umut.soysal.util.LocalStorageUtil.writeToken;
import static com.umut.soysal.util.LocalStorageUtil.writeUrlWebsocket;
import static com.umut.soysal.util.LocalStorageUtil.writeWebsocket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;

import androidx.annotation.NonNull;

import com.umut.soysal.R;
import com.umut.soysal.api.ApiClient;
import com.umut.soysal.api.ApiService;
import com.umut.soysal.api.HealthCheckService;
import com.umut.soysal.modal.AuthClientModal;
import com.umut.soysal.util.LocalStorageUtil;
import com.umut.soysal.util.ToastContainer;

import lombok.Getter;
import lombok.Setter;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LoginActivity extends Activity {
    private EditText accountEditText,passwordEditText;
    private CheckBox rememberCheckBox;
    private AlertDialog dialog;
    private ProgressBar loadingProgressBar;
    private ApiService apiService;
    ToastContainer toastContainer;
    ImageView img_banner_stid;
    Button loginButton;
    String baseUrl;
    String text = "Đăng nhập";

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        apiService = ApiClient.getApiService(getBaseContext());
        toastContainer = new ToastContainer();
        if (isUserAuthenticated(getBaseContext())) {
            redirectToMainActivity();
        }

        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        accountEditText = findViewById(R.id.editTextAccount);
        passwordEditText = findViewById(R.id.editTextPassword);
        rememberCheckBox = findViewById(R.id.checkboxRemember);
        img_banner_stid = findViewById(R.id.img_banner_stid);
        loginButton = findViewById(R.id.buttonLogin);

        loginButton.setOnClickListener(v -> {
                    baseUrl = readBaseUrl(this);
                    if (baseUrl != null && !baseUrl.isEmpty()) {
                        attemptLogin();
                    } else {
                        toastContainer.showToast(this, "Chưa cấu hình CICServer");
                    }
                }
        );

        img_banner_stid.setOnLongClickListener(v -> {
            openModalCICServer();
            return false;
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        baseUrl = readBaseUrl(this);
        if (baseUrl == null) {
            loginButton.setEnabled(false);
            toastContainer.showToast(this, "Chưa cấu hình CIC Server");
        } else {
            loginButton.setEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        baseUrl = readBaseUrl(this);
        if (baseUrl == null) {
            loginButton.setEnabled(false);
            toastContainer.showToast(this, "Chưa cấu hình CIC Server");
        } else {
            loginButton.setEnabled(true);
        }
    }

    private boolean isUserAuthenticated(Context context) {
        String token = LocalStorageUtil.readToken(context);
        return !token.isEmpty();
    }

    private void redirectToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void attemptLogin() {
        String account = accountEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        boolean remember = rememberCheckBox.isChecked();

        String clientName = getClientNameDefault();

        loadingProgressBar.setVisibility(View.VISIBLE);
        Button loginButton = findViewById(R.id.buttonLogin);
        loginButton.setText("");

        AuthClientModal.LoginRequest request = new AuthClientModal.LoginRequest();
        request.setAccount(account);
        request.setPassword(password);
        request.setRemember(remember);
        request.setClientName(clientName);

        Call<AuthClientModal.LoginResponse> loginResponseCall = apiService.login(request);
        loginResponseCall.enqueue(new Callback<AuthClientModal.LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthClientModal.LoginResponse> call, @NonNull Response<AuthClientModal.LoginResponse> response) {
                loadingProgressBar.setVisibility(View.INVISIBLE);
                Button loginButton = findViewById(R.id.buttonLogin);
                loginButton.setText(text);
                AuthClientModal.LoginResponse loginResponse = response.body();
                if (loginResponse != null) {
                    if (loginResponse.getResponse() != null) {
                        writeToken(getBaseContext(), loginResponse.getResponse().getToken());
                        writeClientId(getBaseContext(), loginResponse.getResponse().getClientId());
                        writeClientName(getBaseContext(), loginResponse.getResponse().getClientName());
                        writeIsSuer(getBaseContext(), loginResponse.getResponse().getIssuer());

                        saveAuthenticationStatus(loginResponse.getResponse().getToken());
                        saveProfileUserName(loginResponse.getResponse().getSubject());
                        saveProfileUserName(loginResponse.getResponse().getIssuer());

                        redirectToMainActivity();
                    } else {
                        String message = loginResponse.getMessage();

                        toastContainer.showToast(LoginActivity.this, "Đăng nhập thất bại: " + message);
                    }
                } else {
                    toastContainer.showToast(LoginActivity.this, "Đăng nhập thất bại. Vui lòng thử lại.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthClientModal.LoginResponse> call, @NonNull Throwable t) {
                loadingProgressBar.setVisibility(View.INVISIBLE);
                Button loginButton = findViewById(R.id.buttonLogin);
                loginButton.setText(text);
                toastContainer.showToast(LoginActivity.this, "Lỗi kết nối. Xin vui lòng kiểm tra kết nối Internet của bạn.");
            }
        });
    }

    private void saveAuthenticationStatus(String token) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putString("token", token).apply();
    }

    private void saveProfileUserName(String userName) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putString("userName", userName).apply();
    }

    private void openModalCICServer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cấu hình CICServer");

        View view = getLayoutInflater().inflate(R.layout.cicserver_modal, null);

        Switch mySwitch = view.findViewById(R.id.switchWebsocket);
        EditText urlWebsocket = view.findViewById(R.id.urlWebsocket);
        EditText urlCICServer = view.findViewById(R.id.urlCICServer);
        Button checkHealth = view.findViewById(R.id.checkHealth);
        Button saveWebSocket = view.findViewById(R.id.saveWebSocket);
        Button saveUrlServer = view.findViewById(R.id.saveUrlServer);

        checkHealth.setEnabled(false);
        saveWebSocket.setEnabled(false);

        baseUrl = readBaseUrl(this);
        String webSocketUrl = readUrlWebsocket(this);
        UpdateUrl updateUrl = new UpdateUrl();

        if(baseUrl != null) {
            urlCICServer.setText(baseUrl);
        }
        if(webSocketUrl != null) {
            urlWebsocket.setText(webSocketUrl);
        }
        TextWatcher textWatcherCICServer = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(@NonNull CharSequence s, int start, int before, int count) {
                String newText = s.toString().trim();
                checkHealth.setEnabled(!newText.isEmpty());
            }

            @Override
            public void afterTextChanged(@NonNull Editable s) {
            }
        };

        TextWatcher textWatcherWebsocket = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(@NonNull CharSequence s, int start, int before, int count) {
                String newText = s.toString().trim();
                saveWebSocket.setEnabled(!newText.isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        urlCICServer.addTextChangedListener(textWatcherCICServer);
        urlWebsocket.addTextChangedListener(textWatcherWebsocket);

        checkHealth.setOnClickListener(v -> {
            updateUrl.setNewUrl(urlCICServer.getText().toString());
            if (updateUrl.getNewUrl().startsWith("http://") || updateUrl.getNewUrl().startsWith("https://")) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(updateUrl.getNewUrl())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                HealthCheckService service = retrofit.create(HealthCheckService.class);
                Call<ResponseBody> call = service.getHealth();
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.code() == 200) {
                            checkHealth.setVisibility(View.GONE);
                            saveUrlServer.setVisibility(View.VISIBLE);
                            toastContainer.showToast(LoginActivity.this, "Thành công!");
                        } else {
                            toastContainer.showToast(LoginActivity.this, "Kiểm tra lại url");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        toastContainer.showToast(LoginActivity.this, "Thất bại!");
                    }
                });
            } else {
                toastContainer.showToast(LoginActivity.this, "Thiếu 'http://' hoặc 'https://'");
            }
            checkHealth.setEnabled(false);
        });

        saveUrlServer.setOnClickListener(v -> {
            writeBaseUrl(LoginActivity.this, updateUrl.getNewUrl());
            apiService = ApiClient.getApiService(LoginActivity.this);
            loginButton.setEnabled(true);
            saveUrlServer.setEnabled(false);
            toastContainer.showToast(LoginActivity.this, "Lưu cấu hình thành công");
        });

        saveWebSocket.setOnClickListener(v -> {
            String url = urlWebsocket.getText().toString();
            writeUrlWebsocket(this, url);
            saveWebSocket.setEnabled(false);
            toastContainer.showToast(LoginActivity.this, "Lưu cấu hình thành công");
        });

        Boolean isWebsocket = readWebsocket(this);

        if (mySwitch != null) {
            mySwitch.setChecked(isWebsocket);

            mySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    urlWebsocket.setEnabled(true);
                    writeWebsocket(this, true);
                } else {
                    urlWebsocket.setEnabled(false);
                    writeWebsocket(this, false);
                }
            });
        }


        builder.setView(view);

        builder.setPositiveButton(null, null);

        builder.setNegativeButton("Đóng", (dialog, which) -> {
            dialog.dismiss();
        });

        dialog = builder.create();
        dialog.show();
    }

    @Getter
    @Setter
    public static class UpdateUrl {
        private String newUrl;
    }
}

