package com.umut.soysal.ui;

import static com.umut.soysal.util.ContainerUtil.save_image;
import static com.umut.soysal.util.LocalStorageUtil.clearDataAuthen;
import static com.umut.soysal.util.LocalStorageUtil.readCAN;
import static com.umut.soysal.util.LocalStorageUtil.readQrCode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.common.apiutil.nfc.Nfc;
import com.umut.soysal.R;
import com.umut.soysal.util.PackDataIntoIntent;
import com.umut.soysal.util.ToastContainer;

import net.sf.scuba.smartcards.CardFileInputStream;
import net.sf.scuba.smartcards.CardService;

import org.CICSystemUtil;
import org.CardPersonalData;
import org.P8Service.P8CardService;
import org.jmrtd.PACEKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.icao.DG2File;
import org.service.CardReaderService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


public class SubMainActivity extends AppCompatActivity {
    private static final String PREFERENCE_FILE_KEY = "card_data";
    private View mainLayout, loadingLayout,shadowLayout, errorLayout, whiteLayout, startLayout;
    //    private ProgressBar loadingProgressBar;
    Button repeatBtn;
    String CANNumber, dataQRScan;
    ToastContainer toastContainer;
    private ImageView[] progressDots;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            clearDataAuthen(this);
            finishAffinity();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.sub_main_activity);
        toastContainer = new ToastContainer();

        CANNumber = readCAN(this);
        dataQRScan = readQrCode(this);

        TextView cardNumberTextView = findViewById(R.id.card_number);
        String text = getString(R.string.card_number_hint);
        if (dataQRScan != null) {
            cardNumberTextView.setText(String.format(text, dataQRScan));
        } else {
            cardNumberTextView.setText(String.format(text, CANNumber));
        }

        mainLayout = findViewById(R.id.main_layout);
        loadingLayout = findViewById(R.id.loading_layout);
        shadowLayout = findViewById(R.id.shadow_layout);
        errorLayout = findViewById(R.id.error_layout);
        whiteLayout = findViewById(R.id.white_layout);
        startLayout = findViewById(R.id.start_layout);
        repeatBtn = findViewById(R.id.repeatBtn);

        progressDots = new ImageView[]{
                findViewById(R.id.progress_dot_1),
                findViewById(R.id.progress_dot_2),
                findViewById(R.id.progress_dot_3),
                findViewById(R.id.progress_dot_4),
                findViewById(R.id.progress_dot_5),
                findViewById(R.id.progress_dot_6),
                findViewById(R.id.progress_dot_7),
                findViewById(R.id.progress_dot_8),
        };

        repeatBtn.setOnClickListener(v -> onReadCard());
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    // lấy dl để đọc thẻ
    public void onReadCard() {
        Animation animSlideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        shadowLayout.setVisibility(View.VISIBLE);
        whiteLayout.startAnimation(animSlideUp);
        startLayout.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        repeatBtn.setEnabled(false);
        repeatBtn.setText("");

        PACEKeySpec paceKeySpec = null;
        if (dataQRScan != null) {
            paceKeySpec = PACEKeySpec.createCANKey(dataQRScan);
        } else if (CANNumber != null && !CANNumber.isEmpty()) {
            paceKeySpec = PACEKeySpec.createCANKey(CANNumber);
        }

        if (paceKeySpec == null) {
            whiteLayout.setVisibility(View.GONE);
            loadingLayout.setVisibility(View.GONE);
            shadowLayout.setVisibility(View.GONE);
            repeatBtn.setEnabled(true);
            repeatBtn.setText("Đọc lại");
            toastContainer.showToast(this, "Thiếu dữ liệu cần thiết để đọc thẻ. Vui lòng thử lại!");
            return;
        }

        P8CardService cardService = new P8CardService(new Nfc(getBaseContext()));
        CardReaderTask readerTask = new CardReaderTask(cardService, paceKeySpec);

        // Delay the execution of the card reading task
        new Handler().postDelayed(() -> readerTask.execute(), 500);
    }

    private class CardReaderTask extends AsyncTask<Void, Integer, Exception> {
        private final CardService cardService;
        private final PACEKeySpec keySpec;
        public CardPersonalData cardPersonalData;

        public CardReaderTask(CardService cardService, PACEKeySpec keySpec) {
            this.cardService = cardService;
            this.keySpec = keySpec;
        }

        @Override
        protected Exception doInBackground(Void... voids) {
            try {
                CardReaderService service = new CardReaderService();
                service.connectReader(cardService);

//                publishProgress(10);

                runOnUiThread(() -> {
                    mainLayout.setVisibility(View.GONE);
                    loadingLayout.setVisibility(View.GONE);
                    shadowLayout.setVisibility(View.VISIBLE);
                    whiteLayout.setVisibility(View.VISIBLE);
                    startLayout.setVisibility(View.VISIBLE);
                });

                if (cardService instanceof P8CardService) {
                    cardService.open();


                    runOnUiThread(() -> {
                        mainLayout.setVisibility(View.GONE);
                        startLayout.setVisibility(View.GONE);
                        shadowLayout.setVisibility(View.VISIBLE);
                        whiteLayout.setVisibility(View.VISIBLE);
                        loadingLayout.setVisibility(View.VISIBLE);
                    });
                    publishProgress(0);
                }

                service.initSecureChannel(keySpec);


                publishProgress(10);
                CardFileInputStream dg15In = service.getInputStream(PassportService.EF_DG15);

                byte[] dg15Bytes = service.readBytes(dg15In);
                boolean checkAA = service.checkAA(dg15Bytes);
                if (!checkAA) {
                    throw new Exception("Check AA failed");
                }
                publishProgress(20);

                CardFileInputStream dg13In = service.getInputStream(PassportService.EF_DG13);
                publishProgress(30);
                byte[] dg13Bytes = service.readBytes(dg13In);
                CardPersonalData personalData = CICSystemUtil.readCardPersonalData(dg13Bytes);
                cardPersonalData = personalData;

                CardFileInputStream dg1In = service.getInputStream(PassportService.EF_DG1);
                byte[] dg1Bytes = service.readBytes(dg1In);

                CardFileInputStream dg2In = service.getInputStream(PassportService.EF_DG2);
                publishProgress(40);
                byte[] dg2Bytes = service.readBytes(dg2In);
                DG2File dg2File = new DG2File(new ByteArrayInputStream(dg2Bytes));

                publishProgress(50);
                try {
                    InputStream inputStream = dg2File.getFaceInfos().get(0).getFaceImageInfos().get(0).getImageInputStream();
                    byte[] imageBytes = CICSystemUtil.inputStreamToByteArray(inputStream);
                    personalData.setFaceImage(imageBytes);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    save_image(bitmap, "original_image.jpg");

                } catch (Exception e) {
                    toastContainer.showToast(SubMainActivity.this, "Lỗi: " + e.getMessage());
                }

                CardFileInputStream dg14In = service.getInputStream(PassportService.EF_DG14);
                publishProgress(60);
                byte[] dg14Bytes = service.readBytes(dg14In);


                CardFileInputStream sodIn = service.getInputStream(PassportService.EF_SOD);
                publishProgress(70);
                byte[] sodBytes = service.readBytes(sodIn);


                // Save data to SharedPreferences
                SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor data = sharedPreferences.edit();

                data.putString("dg1", Base64.encodeToString(dg1Bytes, Base64.DEFAULT));
                data.putString("dg2", Base64.encodeToString(dg2Bytes, Base64.DEFAULT));
                data.putString("dg13", Base64.encodeToString(dg13Bytes, Base64.DEFAULT));
                data.putString("dg14", Base64.encodeToString(dg14Bytes, Base64.DEFAULT));
                data.putString("dg15", Base64.encodeToString(dg15Bytes, Base64.DEFAULT));
                data.putString("sod", Base64.encodeToString(sodBytes, Base64.DEFAULT));
                data.putString("card_number", cardPersonalData.getCardNumber());

                data.apply();


                publishProgress(80);  // Completed
            } catch (Exception e) {
                return e;
            } finally {
                cardService.close();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int progress = values[0];
            int step = progress / 10;
            for (int i = 0; i < progressDots.length; i++) {
                if (i < step) {
                    progressDots[i].setImageResource(R.drawable.dot_blue);
                } else {
                    progressDots[i].setImageResource(R.drawable.dot_gray);
                }
            }
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (cardPersonalData != null) {
                Intent intent;
                if (getCallingActivity() != null) {
                    intent = new Intent();
                } else {
                    intent = new Intent(SubMainActivity.this, ActivityValidation.class);
                }

                if (cardPersonalData.getFaceImage() != null) {
                    PackDataIntoIntent.pack(intent, cardPersonalData);

                    if (getCallingActivity() != null) {
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    } else {
                        startActivity(intent);
                    }
                } else {
                    loadingLayout.setVisibility(View.GONE);
                    startLayout.setVisibility(View.GONE);
                    whiteLayout.setVisibility(View.GONE);
                    shadowLayout.setVisibility(View.GONE);
                    mainLayout.setVisibility(View.VISIBLE);
                    repeatBtn.setEnabled(true);
                    repeatBtn.setText("Đọc lại");

                    toastContainer.showToast(SubMainActivity.this, "Chưa đọc được hình ảnh vui lòng thử lại!");
                }
            }

            if (result != null) {
                mainLayout.setVisibility(View.GONE);
                loadingLayout.setVisibility(View.GONE);
                startLayout.setVisibility(View.GONE);
                whiteLayout.setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.VISIBLE);
                repeatBtn.setEnabled(true);
                repeatBtn.setText("Đọc lại");
                toastContainer.showToast(SubMainActivity.this, "Vui lòng kiểm tra mã số thẻ hoặc vị trí đọc thẻ. Và thử lại!");

                new Handler().postDelayed(() -> {
                    whiteLayout.setVisibility(View.GONE);
                    startLayout.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.GONE);
                    shadowLayout.setVisibility(View.GONE);
                    mainLayout.setVisibility(View.VISIBLE);
                }, 2500); // 2000 milliseconds = 2 seconds
            }
        }
    }


}
