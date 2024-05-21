package com.umut.soysal.ui;

import static com.umut.soysal.util.ContainerUtil.deleteFaceImage;
import static com.umut.soysal.util.ContainerUtil.getLastSixDigits;
import static com.umut.soysal.util.ContainerUtil.removeBase64Prefix;
import static com.umut.soysal.util.GetNameDevices.decryptMessage;
import static com.umut.soysal.util.GetNameDevices.getSeriNumber;
import static com.umut.soysal.util.LocalStorageUtil.clearDataAuthed;
import static com.umut.soysal.util.LocalStorageUtil.readBaseUrl;
import static com.umut.soysal.util.LocalStorageUtil.readConfigValidation;
import static com.umut.soysal.util.LocalStorageUtil.readIsSuer;
import static com.umut.soysal.util.LocalStorageUtil.readUrlWebsocket;
import static com.umut.soysal.util.LocalStorageUtil.readVersion;
import static com.umut.soysal.util.LocalStorageUtil.readWebsocket;
import static com.umut.soysal.util.LocalStorageUtil.saveConfigValidation;
import static com.umut.soysal.util.LocalStorageUtil.saveManagerId;
import static com.umut.soysal.util.LocalStorageUtil.saveProfileAccountId;
import static com.umut.soysal.util.LocalStorageUtil.saveProfileAccountName;
import static com.umut.soysal.util.LocalStorageUtil.writeBaseUrl;
import static com.umut.soysal.util.LocalStorageUtil.writeCAN;
import static com.umut.soysal.util.LocalStorageUtil.writeClientName;
import static com.umut.soysal.util.LocalStorageUtil.writeDeviceId;
import static com.umut.soysal.util.LocalStorageUtil.writeQRCode;
import static com.umut.soysal.util.LocalStorageUtil.writeUrlWebsocket;
import static com.umut.soysal.util.LocalStorageUtil.writeVersion;
import static com.umut.soysal.util.LocalStorageUtil.writeWebsocket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.common.apiutil.ResultCode;
import com.common.apiutil.decode.DecodeReader;
import com.common.callback.IDecodeReaderListener;
import com.umut.soysal.R;
import com.umut.soysal.api.ApiClient;
import com.umut.soysal.api.ApiService;
import com.umut.soysal.api.HealthCheckService;
import com.umut.soysal.modal.InformationModal;
import com.umut.soysal.util.FileLogger;
import com.umut.soysal.util.LocalStorageUtil;
import com.umut.soysal.util.PerformLogout;
import com.umut.soysal.util.RefreshToken;
import com.umut.soysal.util.ToastContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@SuppressLint("InflateParams")
//public class MainActivity extends Activity implements IDecodeReaderListener {
    public class MainActivity extends Activity {
    private AlertDialog dialog, firstPopup;
    private ApiService apiService;
    private EditText CANNumberEditText;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 4, REQUEST_CODE_CAMERA_PERMISSION = 5;
    private DecodeReader mDecodeReader;
    private boolean isLegitimacyEnabled = false, isIntegrityEnabled = false, isFaceCompareEnabled = false, isFaceLivenessEnabled = false;
    private long lastToastTime = 0;
    ToastContainer toastContainer;
    PerformLogout performLogout;
    TextView versionText;
    Intent intent;
    Boolean checkIntegrity, checkLegitimacy, checkFaceCompare, faceLivenessCheck, isConnect;
    ImageView setting, imageViewCap;
    Button canButton, qrCodeButton, configValidation, configWebsocket, logout, cancelButton, updateVersion;

    private boolean isUserAuthenticated(Context context) {
        String token = LocalStorageUtil.readToken(context);
        return !token.isEmpty();
    }

    @Override
    public void onBackPressed() {
//        if (mDecodeReader != null) {
//            mDecodeReader.close();
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        requestCameraPermission();
        apiService = ApiClient.getApiService(getBaseContext());
        toastContainer = new ToastContainer();
        performLogout = new PerformLogout(this, apiService);
        isConnect = false;

        if (!isUserAuthenticated(getBaseContext())) {
            intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        deleteFaceImage("face_image.jpg");
        deleteFaceImage("original_image.jpg");
        clearDataAuthed(this);
        writeCAN(this, null);
//        writeQRCode(this, null);

        LayoutInflater.from(this).inflate(R.layout.popup, null);
        initView();
        getInformation();
        getConfigValidations(this);

        fetchDevices(new DevicesCallback() {
            @Override
            public void onDevicesReceived(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("response")) {
                        String base64String = jsonObject.getString("response");
                        byte[] byteArray = Base64.decode(base64String, Base64.DEFAULT);
                        String checkDevice = decryptMessage(byteArray, Objects.requireNonNull(readIsSuer(MainActivity.this)));

                        try {
                            JSONObject jsonResDevice = new JSONObject(checkDevice);

                            if (jsonResDevice.has("data")) {
                                JSONArray jsonArray = jsonResDevice.getJSONArray("data");

                                boolean isDeviceActivated = false;

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject item = jsonArray.getJSONObject(i);

                                    String serial = item.getString("serial");
//                                    String model = item.getString("model");
                                    boolean status = Objects.equals(item.getString("status"), "ACTIVE");

                                    String serialDevice = getSeriNumber();
//                                    if (serialDevice.equals(serial) && clientModel.equals(model) && status) {
                                    if (serialDevice.equals(serial) && status) {

                                        isDeviceActivated = true;
                                        String name = item.getString("name");
                                        String id = item.getString("id");
                                        writeClientName(getBaseContext(), name);
                                        writeDeviceId(getBaseContext(), id);
                                        break;
                                    }
                                }

                                if (!isDeviceActivated) {
                                    showToastAndPerformLogout();
                                }
                            } else {
                                showToastAndPerformLogout();
                            }
                        } catch (JSONException e) {
                            showToastAndPerformLogout();
                        }
                    }
                } catch (Exception e) {
                    showToastAndPerformLogout();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                toastContainer.showToast(MainActivity.this, "Không thể lấy device!");
                performLogout.performLogout();
            }
        });

        setting.setOnClickListener(v -> {
            showModalSetting();
        });

        canButton.setOnClickListener(v -> {
            if (checkLegitimacy || checkFaceCompare) {
//                mDecodeReader.close();
                if (isConnect) {
                    openModal();
                } else {
                    toastContainer.showToast(this, "Vui lòng kết nối lại và khởi động lại ứng dụng để sử dụng dịch vụ");
                }
            } else {
                toastContainer.showToast(this, "Chưa cấu hình xác thực");
            }
        });

//        qrCodeButton.setOnClickListener(v -> {
//            if (checkLegitimacy || checkFaceCompare) {
//                if (isConnect) {
//                    showPopupDialog();
//                } else {
//                    toastContainer.showToast(this, "Vui lòng kết nối lại và khởi động lại ứng dụng để sử dụng dịch vụ");
//                }
//            } else {
//                toastContainer.showToast(this, "Chưa cấu hình xác thực");
//            }
//        });

    }

    private void showToastAndPerformLogout() {
        toastContainer = new ToastContainer();
        toastContainer.showToast(this, "Thiết bị chưa được kích hoạt!");
        performLogout.performLogout();
    }

    private void getConfigValidations(Context context) {
        checkIntegrity = readConfigValidation(context, "checkIntegrity");
        checkLegitimacy = readConfigValidation(context, "checkLegitimacy");
        checkFaceCompare = readConfigValidation(context, "checkFaceCompare");
        faceLivenessCheck = readConfigValidation(context, "faceLivenessCheck");
    }

    private void setVersionText() {
        String ver = readVersion(this);
        String text = "Phiên bản: " + ver;
        versionText.setText(text);
    }

    private void showModalSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        View view = getLayoutInflater().inflate(R.layout.setting_popup, null);
        layout.addView(view);

        logout = view.findViewById(R.id.logout);
        configWebsocket = view.findViewById(R.id.websocket);
        configValidation = view.findViewById(R.id.config);
        updateVersion = view.findViewById(R.id.update);
        versionText = view.findViewById(R.id.version);

        setVersionText();

        configValidation.setOnClickListener(v -> {
            if (firstPopup != null && firstPopup.isShowing()) {
                firstPopup.dismiss();
            }
            openModalValidation();
        });

        configWebsocket.setOnClickListener(v -> {
            if (firstPopup != null && firstPopup.isShowing()) {
                firstPopup.dismiss();
            }
            openModalCICServer();
        });

        logout.setOnClickListener(v -> {
            if (firstPopup != null && firstPopup.isShowing()) {
                firstPopup.dismiss();
            }
            confirmLogout();
        });

        updateVersion.setOnClickListener(v -> {
            if (firstPopup != null && firstPopup.isShowing()) {
                firstPopup.dismiss();
            }
            performLogout.performLogout();
            openOtherApp(MainActivity.this, "com.stid.cicmanagement");
        });

        builder.setView(layout);

        builder.setPositiveButton("Đóng", (dialog, which) -> {
            dialog.dismiss();
        });

        firstPopup = builder.create();
        firstPopup.show();
    }

    public void openOtherApp(@NonNull Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            toastContainer.showToast(this, "Không tìm thấy ứng dụng CIC Installer. Vui lòng liên hệ quản trị!");
        }
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

        String baseUrl = readBaseUrl(MainActivity.this);
        String webSocketUrl = readUrlWebsocket(this);
        UpdateUrl updateUrl = new UpdateUrl();

        if (baseUrl != null) {
            urlCICServer.setText(baseUrl);
        }
        if (webSocketUrl != null) {
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
            public void afterTextChanged(Editable s) {
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
                            toastContainer.showToast(MainActivity.this, "Thành công!");
                        } else {
                            toastContainer.showToast(MainActivity.this, "Kiểm tra lại url");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        toastContainer.showToast(MainActivity.this, "Thất bại!");
                    }
                });
            } else {
                toastContainer.showToast(MainActivity.this, "Thiếu 'http://' hoặc 'https://'");
            }
            checkHealth.setEnabled(false);
        });

        saveUrlServer.setOnClickListener(v -> {
            performLogout.performLogout();
            writeBaseUrl(this, updateUrl.getNewUrl());
            apiService = ApiClient.getApiService(this);
            saveUrlServer.setEnabled(false);
            toastContainer.showToast(this, "Lưu cấu hình thành công, Vui lòng đăng nhập lại");
        });

        saveWebSocket.setOnClickListener(v -> {
            String url = urlWebsocket.getText().toString();

            if (url.startsWith("ws://")) {
                writeUrlWebsocket(this, url);
                saveWebSocket.setEnabled(false);
                toastContainer.showToast(this, "Lưu cấu hình websocket thành công");
            } else {
                toastContainer.showToast(this, "Thiếu 'ws://'");
            }

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

    private void openModalValidation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cấu hình xác thực");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        View view = getLayoutInflater().inflate(R.layout.layout_modal, null);

        CheckBox checkBox1 = view.findViewById(R.id.checkbox1);
        CheckBox checkBox2 = view.findViewById(R.id.checkbox2);
        CheckBox checkBox3 = view.findViewById(R.id.checkbox3);
        CheckBox checkBox4 = view.findViewById(R.id.checkbox4);

        // Thiết lập giá trị mặc định
        checkBox1.setChecked(checkIntegrity);
        checkBox2.setChecked(checkLegitimacy);
        checkBox3.setChecked(checkFaceCompare);
        checkBox4.setChecked(faceLivenessCheck);

        if (!checkBox3.isChecked()) {
            checkBox4.setEnabled(false);
            checkBox4.setChecked(false);
        }

        if (!checkBox2.isChecked()) {
            checkBox1.setEnabled(false);
            checkBox1.setChecked(false);
        }

        checkBox3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkBox4.setEnabled(isChecked);
            checkBox4.setClickable(isChecked);
            if (!isChecked) {
                checkBox4.setChecked(false);
            }
        });

        checkBox2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkBox1.setEnabled(isChecked);
            checkBox1.setClickable(isChecked);
            if (!isChecked) {
                checkBox1.setChecked(false);
            }
        });
        fetchServerInformation(new ServerInfoCallback() {
            @Override
            public void onServerInfoReceived(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("response")) {
                        JSONObject responseObj = jsonObject.getJSONObject("response");

                        String ver = responseObj.getString("version");
                        writeVersion(MainActivity.this, ver);

                        isIntegrityEnabled = responseObj.getBoolean("integrity");
                        isLegitimacyEnabled = responseObj.getBoolean("legitimacy");
                        isFaceCompareEnabled = responseObj.getBoolean("faceCompare");
                        isFaceLivenessEnabled = responseObj.getBoolean("faceLiveness");
                        if (!isIntegrityEnabled && isLegitimacyEnabled) {
                            String text = "Xác thực toàn vẹn dữ liệu & BCA";
                            checkBox2.setText(text);
                        }
                        checkBox1.setVisibility(isIntegrityEnabled ? View.VISIBLE : View.GONE);
                        checkBox2.setVisibility(isLegitimacyEnabled ? View.VISIBLE : View.GONE);
                        checkBox3.setVisibility(isFaceCompareEnabled ? View.VISIBLE : View.GONE);
                        checkBox4.setVisibility(isFaceLivenessEnabled ? View.VISIBLE : View.GONE);
                    }
                } catch (JSONException e) {
                    toastContainer.showToast(MainActivity.this, e.getMessage());
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
        builder.setView(view);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            boolean AcheckIntegrity = checkBox1.isChecked();
            boolean AcheckLegitimacy = checkBox2.isChecked();
            boolean AcheckFaceCompare = checkBox3.isChecked();
            boolean AfaceLivenessCheck = checkBox4.isChecked();
            if (AcheckIntegrity || AcheckLegitimacy || AcheckFaceCompare || AfaceLivenessCheck) {
                saveConfigValidation(this, AcheckIntegrity, AcheckLegitimacy, AcheckFaceCompare, AfaceLivenessCheck);
                toastContainer.showToast(MainActivity.this, "Lưu cấu hình thành công");
            } else {
                toastContainer.showToast(MainActivity.this, "Lưu thất bại, vui lòng chọn ít nhất 1 giá trị.");
            }
            dialog.dismiss();
        });

        builder.setNegativeButton("Đóng", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.setOnDismissListener(dialogInterface -> {
            getConfigValidations(this);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void confirmLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bạn có muốn đăng xuất?");

        builder.setPositiveButton("Có", (dialog, which) -> {
            performLogout.performLogout();
            dialog.dismiss();
        });

        builder.setNegativeButton("Không", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog logoutPopup = builder.create();
        logoutPopup.show();
    }

    private void showPopupDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setTitle("Vui lòng quét mã QR");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Thêm tiêu đề
        TextView titleTextView = new TextView(this);
        String text = "Vui lòng quét mã QR";
        titleTextView.setText(text);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleTextView.setTextColor(Color.BLACK);
        titleTextView.setGravity(Gravity.CENTER);
        titleTextView.setPadding(120, 32, 120, 16);
        layout.addView(titleTextView);

        View view = getLayoutInflater().inflate(R.layout.popup, null);
        layout.addView(view);

        dialog.setContentView(layout);

        cancelButton = new Button(this);
        cancelButton.setText("Hủy");
        cancelButton.setOnClickListener(v -> {
//            mDecodeReader.close();
            dialog.dismiss();
        });

        layout.addView(cancelButton);

        dialog.setOnShowListener(dialogInterface -> {
//            openHardener();
        });

        dialog.setOnDismissListener(dialogInterface -> {
//            mDecodeReader.close();
        });

        dialog.show();

        new Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
//                mDecodeReader.close();
                dialog.dismiss();

            }
        }, 15000);
    }

    private void fetchDevices(DevicesCallback callback) {
        Call<ResponseBody> deviceCall = apiService.getDevices();
        deviceCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        String responseBody = response.body().string();
                        callback.onDevicesReceived(responseBody);
                    } catch (Exception e) {
                        callback.onFailure("Không thể lấy Devices");
                    }
                } else {
                    callback.onFailure("Không thể lấy Devices");
                    toastContainer.showToast(MainActivity.this, "Không thể lấy Devices!");
                    performLogout.performLogout();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onFailure("Lỗi kết nối. Vui lòng kiểm tra lại");

                toastContainer.showToast(MainActivity.this, "Lỗi kết nối. Vui lòng kiểm tra lại!");
                performLogout.performLogout();
            }
        });
    }

    private void fetchServerInformation(ServerInfoCallback callback) {
        Call<ResponseBody> serverInfoCall = apiService.getServerinformation();
        serverInfoCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        String responseBody = response.body().string();
                        callback.onServerInfoReceived(responseBody);
                    } catch (Exception e) {
                        callback.onFailure("Không thể lấy Server Information");
                    }
                } else {
                    callback.onFailure("Không thể lấy Server Information");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onFailure("Lỗi kết nối. Vui lòng kiểm tra lại");
            }
        });
    }

//    @Override
//    public void onRecvData(byte[] bytes) {
//        Toast.makeText(MainActivity.this, Arrays.toString(bytes), Toast.LENGTH_SHORT).show();
//    }

    public interface DevicesCallback {
        void onDevicesReceived(String response) throws JSONException;

        void onFailure(String errorMessage);

    }

    public interface ServerInfoCallback {
        void onServerInfoReceived(String response);

        void onFailure(String errorMessage);

    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA_PERMISSION);
        }
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestStoragePermission();
            } else {
                requestStoragePermission();
            }
        }
    }

    private void getInformation() {
        Call<InformationModal> informationRes = apiService.information();
        informationRes.enqueue(new Callback<InformationModal>() {
            @Override
            public void onResponse(@NonNull Call<InformationModal> call, @NonNull Response<InformationModal> response) {
                InformationModal res = response.body();
                imageViewCap = findViewById(R.id.view_photo_account);
                if (res != null) {
                    if (res.getStatus() == 401 || res.getStatus() == 403) {
                        handleUnauthorizedOrForbidden();
                    }
                    if (res.getResponse() != null) {
                        isConnect = true;
                        TextView textUserNameAccount = findViewById(R.id.textUserNameAccount);
                        textUserNameAccount.setText(res.getResponse().getProfile().getLogin());
                        if (res.getResponse().getProfile().getEmail() != null) {
                            TextView textEmailAccount = findViewById(R.id.textEmailAccount);
                            textEmailAccount.setText(res.getResponse().getProfile().getEmail());
                        }
                        saveProfileAccountId(MainActivity.this, res.getResponse().getProfile().getId());
                        saveManagerId(MainActivity.this, res.getResponse().getQuota().getAccountId().toString());
                        saveProfileAccountName(MainActivity.this, res.getResponse().getProfile().getLogin());
                        String base64Image = res.getResponse().getProfile().getImageBase64();
                        if (base64Image != null) {
                            String decodedString = removeBase64Prefix(base64Image);
                            Bitmap bitmap = null;
                            if (!decodedString.isEmpty()) {
                                byte[] decodedBytesCap = Base64.decode(decodedString, Base64.DEFAULT);
                                bitmap = BitmapFactory.decodeByteArray(decodedBytesCap, 0, decodedBytesCap.length);
                            }
                            if (bitmap != null) {
                                imageViewCap.setImageBitmap(bitmap);
                            } else {
                                imageViewCap.setImageResource(R.drawable.photo);
                            }
                        } else {
                            imageViewCap.setImageResource(R.drawable.photo);
                        }
                    }
                } else {
                    CardView cardView = findViewById(R.id.parent_layout);
                    cardView.setVisibility(View.GONE);

                    TextView textEmailAccount = findViewById(R.id.textEmailAccount);
                    String text = "Lỗi kết nối CICServer. Vui lòng kiểm tra lại đường truyền hoặc Internet";
                    toastContainer.showToast(MainActivity.this, text);
                    textEmailAccount.setText(text);
                }

            }

            private void handleUnauthorizedOrForbidden() {
                RefreshToken.refreshToken(MainActivity.this, apiService);
                String text = "Tài khoản hiện đang được đăng nhập ở thiết bị khác, vui lòng đăng xuất và thử lại.";
                toastContainer.showToast(MainActivity.this, text);
            }

            @Override
            public void onFailure(@NonNull Call<InformationModal> call, @NonNull Throwable t) {
                CardView cardView = findViewById(R.id.parent_layout);
                cardView.setVisibility(View.GONE);

                TextView textEmailAccount = findViewById(R.id.textEmailAccount);
                String text = "Lỗi kết nối CICServer. Vui lòng kiểm tra lại đường truyền hoặc Internet";
                textEmailAccount.setText(text);
                toastContainer.showToast(MainActivity.this, text);
            }
        });
    }

    private void openModal() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập 6 số cuối CCCD");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        View view = getLayoutInflater().inflate(R.layout.can_modal, null);
        CANNumberEditText = view.findViewById(R.id.editTextCANNumber);

        CANNumberEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(6);
        CANNumberEditText.setFilters(filters);

        CANNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(charSequence.length() == 6);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        builder.setView(view);
        if (checkFaceCompare) {
            intent = new Intent(MainActivity.this, CameraActivity.class);
        } else {
            intent = new Intent(MainActivity.this, SubMainActivity.class);
        }
        builder.setPositiveButton("Đọc thẻ", (dialog, which) -> {
            String CANNumber = CANNumberEditText.getText().toString();

            if (CANNumber.matches("\\d{6}")) {
                writeCAN(this, CANNumber);
                startActivity(intent);
            } else {
                toastContainer.showToast(MainActivity.this, "Vui lòng nhập đúng 6 số cuối CCCD");
            }
            dialog.dismiss();
        });

        builder.setNegativeButton("Đóng", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.setOnCancelListener(dialog -> {
            intent.putExtra("CANNumber", "");
        });

        dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        });

        dialog.show();
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (mDecodeReader == null) {
//            mDecodeReader = new DecodeReader(this);
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (mDecodeReader != null) {
//            mDecodeReader.close();
//        }
//    }
//
//    public void openHardener() {
//        int ret = mDecodeReader.open(115200);
//        if (ret == ResultCode.SUCCESS) {
//            mDecodeReader.setDecodeReaderListener(listener);
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//    }
//
//    private final IDecodeReaderListener listener = data -> {
//        try {
//            if (data != null) {
//                final String dataQRScan = new String(data, StandardCharsets.UTF_8);
//                if (dataQRScan.length() - dataQRScan.replace("|", "").length() != 6) {
//                    runOnUiThread(() -> showToast("Mã QR không đúng định dạng"));
//                    return;
//                }
//
//                String CANQr;
//                if (!dataQRScan.isEmpty()) {
//                    String[] parts = dataQRScan.split("\\|");
//
//                    if (parts.length > 0) {
//                        CANQr = getLastSixDigits(parts[0]);
//                        if (CANQr.matches("\\d{6}")) {
//                            writeQRCode(this, CANQr);
//                            runOnUiThread(() -> navigateToSubMainActivity(this));
//                        } else {
//                            runOnUiThread(() -> showToast("Mã QR không đúng định dạng"));
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            runOnUiThread(() -> showToast("Đã xảy ra lỗi khi đọc mã QR"));
//            FileLogger.logToFile(this, "Error:" + e.getMessage());
//        }
//    };

    private void showToast(String message) {
        long currentTime = System.currentTimeMillis();
        long TOAST_INTERVAL = 3000;
        if (currentTime - lastToastTime > TOAST_INTERVAL) {
            toastContainer.showToast(MainActivity.this, message);
            lastToastTime = currentTime;
        }
    }

//    private void navigateToSubMainActivity(Context context) {
//        mDecodeReader.close();
//        getConfigValidations(context);
//        if (checkFaceCompare) {
//            intent = new Intent(context, CameraActivity.class);
//        } else {
//            intent = new Intent(context, SubMainActivity.class);
//        }
//        startActivity(intent);
//    }


    private void initView() {
        canButton = findViewById(R.id.buttonCAN);
//        qrCodeButton = findViewById(R.id.buttonQrCode);
        setting = findViewById(R.id.setting);
    }

    @Getter
    @Setter
    public static class UpdateUrl {
        private String newUrl;
    }
}