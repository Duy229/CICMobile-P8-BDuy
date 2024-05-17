package com.umut.soysal.util;

import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.umut.soysal.R;

import org.CardPersonalData;

public class PackDataIntoIntent {

    public static final String KEY_CARD_NUMBER = "cardNumber";
    public static final String KEY_MRZ = "mrz";
    public static final String KEY_FACE_IMAGE = "faceImage";
    public static final String KEY_FULL_NAME = "fullName";
    public static final String KEY_DATE_OF_BIRTH = "dateOfBirth";
    public static final String KEY_SEX = "sex";
    public static final String KEY_NATIONALITY = "nationality";
    public static final String KEY_ETHNICITY = "ethnicity";
    public static final String KEY_RELIGION = "religion";
    public static final String KEY_PLACE_OF_ORIGIN = "placeOfOrigin";
    public static final String KEY_PLACE_OF_RESIDENCE = "placeOfResidence";
    public static final String KEY_PERSONAL_IDENTIFICATION = "personalIdentification";
    public static final String KEY_DATE_OF_ISSUE = "dateOfIssue";
    public static final String KEY_DATE_OF_EXPIRY = "dateOfExpiry";
    public static final String KEY_FATHER_NAME = "fatherName";
    public static final String KEY_MOTHER_NAME = "motherName";
    public static final String KEY_SPOUSE_NAME = "spouseName";
    public static final String KEY_OLD_CARD_NUMBER = "oldCardNumber";

    public static void pack(Intent intent, CardPersonalData cardPersonalData) {
        if (intent == null || cardPersonalData == null) {
            return;
        }
        intent.putExtra(KEY_CARD_NUMBER, cardPersonalData.getCardNumber());
        intent.putExtra(KEY_OLD_CARD_NUMBER, cardPersonalData.getOldCardNumber());
        intent.putExtra(KEY_FULL_NAME, cardPersonalData.getFullName());
        intent.putExtra(KEY_DATE_OF_BIRTH, cardPersonalData.getDateOfBirth());
        intent.putExtra(KEY_SEX, cardPersonalData.getSex());
        intent.putExtra(KEY_NATIONALITY, cardPersonalData.getNationality());
        intent.putExtra(KEY_ETHNICITY, cardPersonalData.getEthnicity());
        intent.putExtra(KEY_RELIGION, cardPersonalData.getReligion());
        intent.putExtra(KEY_PLACE_OF_ORIGIN, cardPersonalData.getPlaceOfOrigin());
        intent.putExtra(KEY_PLACE_OF_RESIDENCE, cardPersonalData.getPlaceOfResidence());
        intent.putExtra(KEY_PERSONAL_IDENTIFICATION, cardPersonalData.getPersonalIdentification());
        intent.putExtra(KEY_DATE_OF_ISSUE, cardPersonalData.getDateOfIssue());
        intent.putExtra(KEY_DATE_OF_EXPIRY, cardPersonalData.getDateOfExpiry());

        intent.putExtra(KEY_FATHER_NAME, cardPersonalData.getFatherName());
        intent.putExtra(KEY_MOTHER_NAME, cardPersonalData.getMotherName());
        intent.putExtra(KEY_SPOUSE_NAME, cardPersonalData.getSpouseName());
    }

    public static void displayData(Activity activity, @NonNull Intent intent, Boolean relativeInformation) {
        displayTextView(activity, R.id.output_card_number, intent.getStringExtra(KEY_CARD_NUMBER));
        displayTextView(activity, R.id.output_old_card_number, intent.getStringExtra(KEY_OLD_CARD_NUMBER));
        displayTextView(activity, R.id.output_full_name, intent.getStringExtra(KEY_FULL_NAME));
        displayTextView(activity, R.id.output_date_of_birth, intent.getStringExtra(KEY_DATE_OF_BIRTH));
        displayTextView(activity, R.id.output_sex, intent.getStringExtra(KEY_SEX));
        displayTextView(activity, R.id.output_nationality, intent.getStringExtra(KEY_NATIONALITY));
        displayTextView(activity, R.id.output_ethnicity, intent.getStringExtra(KEY_ETHNICITY));
        displayTextView(activity, R.id.output_religion, intent.getStringExtra(KEY_RELIGION));
        displayTextView(activity, R.id.output_place_of_origin, intent.getStringExtra(KEY_PLACE_OF_ORIGIN));
        displayTextView(activity, R.id.output_place_of_residence, intent.getStringExtra(KEY_PLACE_OF_RESIDENCE));
        displayTextView(activity, R.id.output_personal_identification, intent.getStringExtra(KEY_PERSONAL_IDENTIFICATION));
        displayTextView(activity, R.id.output_date_of_issue, intent.getStringExtra(KEY_DATE_OF_ISSUE));
        displayTextView(activity, R.id.output_date_of_expiry, intent.getStringExtra(KEY_DATE_OF_EXPIRY));
        if(relativeInformation != null && relativeInformation) {
            displayTextView(activity, R.id.output_date_of_expiry, intent.getStringExtra(KEY_FATHER_NAME));
            displayTextView(activity, R.id.output_date_of_expiry, intent.getStringExtra(KEY_MOTHER_NAME));
            displayTextView(activity, R.id.output_date_of_expiry, intent.getStringExtra(KEY_SPOUSE_NAME));
        }
    }

    private static void displayTextView(@NonNull Activity activity, int textViewId, String text) {
        TextView textView = activity.findViewById(textViewId);
        if (textView != null && text != null) {
            textView.setText(text);
        }
    }
}
