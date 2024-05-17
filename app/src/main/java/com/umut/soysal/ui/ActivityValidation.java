package com.umut.soysal.ui;

import static com.umut.soysal.util.ContainerUtil.loadBitmapFromStorage;
import static com.umut.soysal.util.ConvertUtil.convertBitmapToBase64;
import static com.umut.soysal.util.LocalStorageUtil.clearDataAuthen;
import static com.umut.soysal.util.LocalStorageUtil.readCardData;
import static com.umut.soysal.util.LocalStorageUtil.readConfigValidation;
import static com.umut.soysal.util.LocalStorageUtil.readDeviceId;
import static com.umut.soysal.util.LocalStorageUtil.readWebsocket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.umut.soysal.R;
import com.umut.soysal.api.ApiClient;
import com.umut.soysal.api.ApiService;
import com.umut.soysal.modal.CardDataModel;
import com.umut.soysal.modal.ValidationModal;
import com.umut.soysal.modal.WebSocketModal;
import com.umut.soysal.util.ConvertUtil;
import com.umut.soysal.util.LocalStorageUtil;
import com.umut.soysal.util.PackDataIntoIntent;
import com.umut.soysal.util.ToastContainer;
import com.umut.soysal.util.WebSocketClientService;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityValidation extends AppCompatActivity {
    private View validationLayout, onFailure;
    private ApiService apiService;
    private WebSocketClientService webSocketClientService;
    LinearLayout loading_vailidation1, loading_vailidation2, loading_vailidation3, loading_vailidation4;
    TextView textViewCheckIntegrity, textViewCheckOrigin, textViewCheckOwner, textViewFaceLivenessCheck, errorTextView;
    String base64ImageCap, json, notChecked = "Không kiểm tra", valid = "Hợp lệ", invalid = "Không hợp lệ", serviceError = "Lỗi dịch vụ";
    ToastContainer toastContainer;
    ImageView faceCapture, originalCapture;
    Boolean checkIntegrity, checkLegitimacy, checkFaceCompare, faceLivenessCheck, relativeInformation;
    Button homeButton;

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.validation_activity);
        apiService = ApiClient.getApiService(getBaseContext());
        toastContainer = new ToastContainer();

        validationLayout = findViewById(R.id.activity_layout);
        originalCapture = findViewById(R.id.view_photo);
        faceCapture = findViewById(R.id.view_photo_cap);
        textViewCheckIntegrity = findViewById(R.id.textViewCheckIntegrity);
        textViewCheckOrigin = findViewById(R.id.textViewCheckOrigin);
        textViewCheckOwner = findViewById(R.id.textViewCheckOwner);
        textViewFaceLivenessCheck = findViewById(R.id.textViewFaceLivenessCheck);
        loading_vailidation1 = findViewById(R.id.loading_vailidation1);
        loading_vailidation2 = findViewById(R.id.loading_vailidation2);
        loading_vailidation3 = findViewById(R.id.loading_vailidation3);
        loading_vailidation4 = findViewById(R.id.loading_vailidation4);
        onFailure = findViewById(R.id.onFailure);
        homeButton = findViewById(R.id.home_button);
        errorTextView = findViewById(R.id.error);

        homeButton.setOnClickListener(v -> {
            clearDataAuthen(ActivityValidation.this);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        Bitmap originalImage = loadBitmapFromStorage("original_image.jpg");
        if (originalImage != null) {
            originalCapture.setImageBitmap(originalImage);
        }

        Bitmap captureImage = loadBitmapFromStorage("face_image.jpg");
        if (captureImage != null) {
            base64ImageCap = convertBitmapToBase64(captureImage);
            faceCapture.setImageBitmap(captureImage);
        }

        Intent intent = getIntent();
        PackDataIntoIntent.displayData(ActivityValidation.this, intent, relativeInformation);

        // Nhận dữ liệu từ Intent
        checkIntegrity = readConfigValidation(this, "checkIntegrity");
        checkLegitimacy = readConfigValidation(this, "checkLegitimacy");
        checkFaceCompare = readConfigValidation(this, "checkFaceCompare");
        faceLivenessCheck = readConfigValidation(this, "faceLivenessCheck");

        // Tạo dữ liệu request
        CardDataModel cardDataModel = readCardData(this);
        ValidationModal.ValidationRequest request = new ValidationModal.ValidationRequest();
        request.setCardNumber(cardDataModel.getCardNumber());
        request.setCardRaw(cardDataModel);

        ValidationModal.CardValidation cardValidation = new ValidationModal.CardValidation();
        cardValidation.setLegitimacyValidation(null);
        if (checkIntegrity || checkLegitimacy) {
            cardValidation.setLegitimacyValidation(checkLegitimacy);
        } else {
            cardValidation.setLegitimacyValidation(false);
        }
        request.setCardValidation(cardValidation);

        if (checkFaceCompare) {
            ValidationModal.ImageValidation imageValidation = new ValidationModal.ImageValidation();
            imageValidation.setCapturedImage(base64ImageCap); //Set
            imageValidation.setCardBackImage(null);
            imageValidation.setCardFrontImage(null);
            imageValidation.setAppRecordedVideo(null);
            imageValidation.setFaceLivenessCheck(faceLivenessCheck); //Set
            imageValidation.setLiveRecordedVideo(null);
            request.setImageValidation(imageValidation);
        }

        String createdBy = LocalStorageUtil.readProfileUsername(this);
        UUID accountId = UUID.fromString(LocalStorageUtil.readProfileAccountId(this));
        UUID managerId = UUID.fromString(LocalStorageUtil.readManagerId(this));
        String deviceName = "VTCs_ID33.1";

        request.setCreatedBy(createdBy);
        request.setAccountId(accountId);
        request.setDeviceName(deviceName);
        request.setManagerId(managerId);
        //Call Api
        performValidation(request);
        fetchServerInformation();
    }

    private void fetchServerInformation() {
        Call<ResponseBody> serverInfoCall = apiService.getServerinformation();
        serverInfoCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            String responseBody = response.body().string();
                            handleServerInformation(responseBody);
                        } else {
                            toastContainer.showToast(ActivityValidation.this, "Không thể lấy Server Information.");
                        }
                    } catch (Exception e) {
                        toastContainer.showToast(ActivityValidation.this, "Không thể lấy Server Information.");
                    }
                } else {
                    toastContainer.showToast(ActivityValidation.this, "Không thể lấy Server Information");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                toastContainer.showToast(ActivityValidation.this, "Lỗi kết nối. Vui lòng kiểm tra lại");
            }
        });
    }


    @SuppressLint("SetTextI18n")
    private void handleServerInformation(String responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            if (jsonObject.has("response")) {
                JSONObject responseObj = jsonObject.getJSONObject("response");

                boolean isLegitimacyEnabled = responseObj.getBoolean("legitimacy");
                boolean isIntegrityEnabled = responseObj.getBoolean("integrity");
                boolean isFaceCompareEnabled = responseObj.getBoolean("faceCompare");
                boolean isFaceLivenessEnabled = responseObj.getBoolean("faceLiveness");
                relativeInformation = responseObj.getBoolean("relativeInformation");

                LinearLayout viewCheckIntegrity = findViewById(R.id.viewIntegrity);
                LinearLayout viewCheckLegitimacy = findViewById(R.id.viewLegitimacy);
                LinearLayout viewCheckFaceCompare = findViewById(R.id.viewFaceCompare);
                LinearLayout viewFaceLivenessCheck = findViewById(R.id.viewFaceLiveness);
                TextView labelLegitimacy = findViewById(R.id.labelLegitimacy);

                if (!isLegitimacyEnabled && isIntegrityEnabled) {
                    labelLegitimacy.setText("Xác thực toàn vẹn dữ liệu & BCA");
                }

                viewCheckIntegrity.setVisibility(isLegitimacyEnabled ? View.VISIBLE : View.GONE);
                viewCheckLegitimacy.setVisibility(isIntegrityEnabled ? View.VISIBLE : View.GONE);
                viewCheckFaceCompare.setVisibility(isFaceCompareEnabled ? View.VISIBLE : View.GONE);
                viewFaceLivenessCheck.setVisibility(isFaceLivenessEnabled ? View.VISIBLE : View.GONE);
            }
        } catch (JSONException e) {
            Log.e("TAG", "Error message", e);
        }
    }


    private void performValidation(ValidationModal.ValidationRequest request) {
        showProgressBar();

        Call<ValidationModal.ValidationResponse> ValidationResponseCall = apiService.validation(request);
        ValidationResponseCall.enqueue(new Callback<ValidationModal.ValidationResponse>() {
            @Override
            public void onResponse(@NonNull Call<ValidationModal.ValidationResponse> call, @NonNull Response<ValidationModal.ValidationResponse> response) {
                ValidationModal.ValidationResponse validationResponse = response.body();
                if (validationResponse != null) {
                    if (validationResponse.getStatus() == 200 || validationResponse.getResponse() != null) {
                        Boolean isWebsocket = readWebsocket(ActivityValidation.this);
                        WebSocketModal.OnSuccessFormat dataWebsocket = convertDataSuccess(validationResponse);
                        json = ConvertUtil.toJson(dataWebsocket);
                        if (isWebsocket && json != null) {
                            webSocketClientService = new WebSocketClientService();
                            webSocketClientService.createWebSocketClient(ActivityValidation.this, json);

                            toastContainer.showToast(ActivityValidation.this, "Xác thực & truyền dữ liệu thành công.");
                        } else {
                            toastContainer.showToast(ActivityValidation.this, "Xác thực thành công.");
                        }
                        json = null;

                        String legitimacyStatus;
                        if (validationResponse.getResponse().getCardLegitimacyResult() != null) {
                            Boolean isLegitimacyResult = validationResponse.getResponse().getCardLegitimacyResult().getResult();
                            legitimacyStatus = isLegitimacyResult ? valid : invalid;
                            if (isLegitimacyResult) {
                                if(checkIntegrity) {
                                    textViewCheckIntegrity.setTextColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
                                }
                                textViewCheckOrigin.setTextColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
                            } else {
                                if(checkIntegrity) {
                                    textViewCheckIntegrity.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
                                }
                                textViewCheckOrigin.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
                            }
                        } else {
                            legitimacyStatus = notChecked;
                        }
                        textViewCheckOrigin.setText(legitimacyStatus);
                        if(checkIntegrity) {
                            textViewCheckIntegrity.setText(legitimacyStatus);
                        } else {
                            textViewCheckIntegrity.setText(notChecked);
                        }

                        String imageStatus = "";
                        if (validationResponse.getResponse().getFaceCompareResult() != null) {
                            Boolean isImageResult = validationResponse.getResponse().getFaceCompareResult().getResult();
                            if (isImageResult != null) {
                                if (isImageResult) {
                                    String originalString = validationResponse.getResponse().getFaceCompareResult().getMessage();
                                    String textPart = originalString.replaceAll("[0-9.]*%", "");
                                    String numberPart = originalString.replaceAll("[^0-9.]", "");
                                    String formattedNumber = String.format(Locale.getDefault(), "%.2f", Double.parseDouble(numberPart));
                                    imageStatus = textPart + formattedNumber + "%";
                                    textViewCheckOwner.setTextColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
                                } else {
                                    imageStatus = validationResponse.getResponse().getFaceCompareResult().getMessage();
                                    textViewCheckOwner.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
                                }
                            } else {
                                imageStatus = serviceError;
                                textViewCheckOwner.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
                            }

                        } else {
                            imageStatus = notChecked;
                        }
                        textViewCheckOwner.setText(imageStatus);

                        ValidationModal.ValidationResponse.Response.FaceLivenessResult faceLivenessResult = validationResponse.getResponse().getFaceLivenessResult();
                        String faceLivenessStatus;
                        if (faceLivenessResult != null) {
                            if (faceLivenessResult.getResult() != null) {
                                faceLivenessStatus = faceLivenessResult.getMessage() != null ? faceLivenessResult.getMessage() : faceLivenessResult.getResult() ? "Người thật" : "Không phải người thật";
                                if (faceLivenessResult.getResult()) {
                                    textViewFaceLivenessCheck.setTextColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
                                } else {
                                    textViewFaceLivenessCheck.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
                                }
                            } else {
                                faceLivenessStatus = serviceError;
                                textViewFaceLivenessCheck.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
                            }
                        } else {
                            faceLivenessStatus = notChecked;
                        }
                        textViewFaceLivenessCheck.setText(faceLivenessStatus);
                    } else {
                        String error = validationResponse.getError();
                        String message = validationResponse.getMessage();
                        String errorText = "Lỗi: " + error + "Message " + message;
                        errorTextView.setText(errorText);
                    }
                    hideProgressBar();
                } else {
                    onFailureView();
                    toastContainer.showToast(ActivityValidation.this, "Xác thực thất bại. Vui lòng thử lại.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ValidationModal.ValidationResponse> call, @NonNull Throwable t) {
                onFailureView();
                TextView errorTextView = findViewById(R.id.error);

                if (isNetworkAvailable()) {
                    String errorText = "Lỗi: " + t;
                    errorTextView.setText(errorText);
                } else {
                    String errorText = "Lỗi kết nối CICServer. Vui lòng kiểm tra lại đường truyền hoặc Internet";
                    errorTextView.setText(errorText);
                }

                toastContainer.showToast(ActivityValidation.this, "Xác thực thất bại. Vui lòng thử lại.");
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void showProgressBar() {
        onFailure.setVisibility(View.GONE);
        validationLayout.setVisibility(View.VISIBLE);

        loading_vailidation1.setVisibility(View.VISIBLE);
        loading_vailidation2.setVisibility(View.VISIBLE);
        loading_vailidation3.setVisibility(View.VISIBLE);
        loading_vailidation4.setVisibility(View.VISIBLE);

        textViewCheckIntegrity.setVisibility(View.GONE);
        textViewCheckOrigin.setVisibility(View.GONE);
        textViewCheckOwner.setVisibility(View.GONE);
        textViewFaceLivenessCheck.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        onFailure.setVisibility(View.GONE);
        validationLayout.setVisibility(View.VISIBLE);

        loading_vailidation1.setVisibility(View.GONE);
        loading_vailidation2.setVisibility(View.GONE);
        loading_vailidation3.setVisibility(View.GONE);
        loading_vailidation4.setVisibility(View.GONE);

        textViewCheckIntegrity.setVisibility(View.VISIBLE);
        textViewCheckOrigin.setVisibility(View.VISIBLE);
        textViewCheckOwner.setVisibility(View.VISIBLE);
        textViewFaceLivenessCheck.setVisibility(View.VISIBLE);
    }

    private void onFailureView() {
        validationLayout.setVisibility(View.GONE);
        onFailure.setVisibility(View.VISIBLE);
    }

    private WebSocketModal.OnSuccessFormat convertDataSuccess(ValidationModal.ValidationResponse validationResponse) {
        ValidationModal.ValidationResponse.PersonalData data = validationResponse.getResponse().getPersonalData();
        ValidationModal.ValidationResponse.Response validData = validationResponse.getResponse();

        WebSocketModal.OnSuccessFormat successFormat = new WebSocketModal.OnSuccessFormat();
        successFormat.setTimestamp(Instant.now());

        WebSocketModal.OnSuccessFormat.Success success = new WebSocketModal.OnSuccessFormat.Success();

        @NonNull
        WebSocketModal.OnSuccessFormat.Success.Validation validation = new WebSocketModal.OnSuccessFormat.Success.Validation();
        validation.setId(validData.getTransactionId());
        validation.setCreatedDate(validationResponse.getTimestamp());
        validation.setCardIntegrityResult(validData.getCardIntegrityResult() != null ? validData.getCardIntegrityResult().getResult() : null);
        validation.setCardIntegrityMessage(validData.getCardIntegrityResult() != null ? validData.getCardIntegrityResult().getMessage() : null);
        validation.setCardLegitimacyResult(validData.getCardLegitimacyResult() != null ? validData.getCardLegitimacyResult().getResult() : null);
        validation.setCardLegitimacyMessage(validData.getCardLegitimacyResult() != null ? validData.getCardLegitimacyResult().getMessage() : null);
        validation.setFaceCompareAccuracy(validData.getImageAccuracy() != null ? validData.getImageAccuracy().floatValue() : null);
        validation.setFaceCompareResult(validData.getFaceCompareResult() != null ? validData.getFaceCompareResult().getResult() : null);
        validation.setFaceCompareMessage(validData.getFaceCompareResult() != null ? validData.getFaceCompareResult().getMessage() : null);
        validation.setFaceLivenessResult(validData.getFaceLivenessResult() != null ? validData.getFaceLivenessResult().getResult() : null);
        validation.setFaceLivenessMessage(validData.getFaceLivenessResult() != null ? validData.getFaceLivenessResult().getMessage() : null);


        WebSocketModal.OnSuccessFormat.Success.Session session = new WebSocketModal.OnSuccessFormat.Success.Session();
        session.setClientName("Alps P8");
        session.setDeviceName("VTCs_ID_33.1");
        session.setDeviceSerial(readDeviceId(getApplicationContext()));

        success.setPersonalData(WebSocketModal.OnSuccessFormat.Success.PersonalData.from(data, base64ImageCap));
        success.setValidation(validation);
        success.setSession(session);

        successFormat.setSuccess(success);

        return successFormat;
    }
}