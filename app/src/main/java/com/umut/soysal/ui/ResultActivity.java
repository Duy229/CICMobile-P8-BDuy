
package com.umut.soysal.ui;

import static com.umut.soysal.util.LocalStorageUtil.clearDataAuthen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;

import com.umut.soysal.R;
import com.umut.soysal.util.LocalStorageUtil;
//import com.umut.soysal.util.PackDataIntoIntent;
import com.umut.soysal.util.ToastContainer;

public class ResultActivity extends AppCompatActivity {
    ToastContainer toastContainer;
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

    @Override
    public void onBackPressed() {
        clearDataAuthen(this);
        finishAffinity();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.result_activity);
        toastContainer = new ToastContainer();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Boolean checkIntegrity = LocalStorageUtil.getCheckIntegrity(ResultActivity.this);
        Boolean checkLegitimacy = LocalStorageUtil.getCheckLegitimacy(ResultActivity.this);
        Boolean checkFaceCompare = LocalStorageUtil.getCheckFaceCompare(ResultActivity.this);
        Boolean faceLivenessCheck = LocalStorageUtil.getCheckFaceLivenessCheck(ResultActivity.this);

        Button validation = findViewById(R.id.modal_button);

        validation.setOnClickListener(v -> {
            if (!checkIntegrity && !checkFaceCompare && !checkLegitimacy && !faceLivenessCheck) {
                toastContainer.showToast(this, "Chưa chọn cấu hình xác thực");
            } else {
                Intent intent;
                if (checkFaceCompare) {
                    intent = new Intent(ResultActivity.this, CameraActivity.class);
                } else {
                    intent = new Intent(ResultActivity.this, ActivityValidation.class);
                }
                intent.putExtra("checkIntegrity", checkIntegrity);
                intent.putExtra("checkLegitimacy", checkLegitimacy);
                intent.putExtra("checkFaceCompare", checkFaceCompare);
                intent.putExtra("faceLivenessCheck", faceLivenessCheck);
                startActivity(intent);
            }
        });
//        Intent intent = getIntent();
//        PackDataIntoIntent.displayData(this, intent);
    }


}