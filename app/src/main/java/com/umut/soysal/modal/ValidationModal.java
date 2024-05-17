package com.umut.soysal.modal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationModal {
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class ValidationRequest {
        private String cardNumber;
        private CardDataModel cardRaw;
        private CardValidation cardValidation;
        private ImageValidation imageValidation;
        private String deviceName;
        private UUID accountId;
        private String createdBy;
        private UUID managerId;
    }

    @Getter
    @Setter
    public static class CardValidation {
        private Boolean legitimacyValidation;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class ImageValidation {
        private String capturedImage;
        private String cardFrontImage;
        private String cardBackImage;
        private String appRecordedVideo;
        private String liveRecordedVideo;
        private boolean faceLivenessCheck;
    }

    // Response
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValidationResponse {
        private Instant timestamp;
        private int status;
        private Response response;
        private String error;
        private String message;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Getter
        @Setter
        public static class Response {
            private String transactionId;
            private Boolean integrityResult;
            private String integrityMessage;
            private Boolean legitimacyResult;
            private String legitimacyMessage;
            private Boolean imageResult;
            private String imageMessage;
            private Double imageAccuracy;
            private PersonalData personalData;
            private CardIntegrityResult cardIntegrityResult;
            private CardLegitimacyResult cardLegitimacyResult;
            private FaceCompareResult faceCompareResult;
            private FaceLivenessResult faceLivenessResult;

            @JsonIgnoreProperties(ignoreUnknown = true)
            @Getter
            @Setter
            public static class CardIntegrityResult {
                private Instant time;
                private String id;
                private Boolean result;
                private String message;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            @Getter
            @Setter
            public static class CardLegitimacyResult {
                private Instant time;
                private String id;
                private Boolean result;
                private String message;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            @Getter
            @Setter
            public static class FaceCompareResult {
                private Instant time;
                private String id;
                private Boolean result;
                private String message;
                private int accuracy;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            @Getter
            @Setter
            public static class FaceLivenessResult {
                private Instant time;
                private String id;
                private Boolean result;
                private String message;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Getter
        @Setter
        public static class PersonalData {
            private String mrz;
            private String faceImage;
            private String cardNumber;
            private String fullName;
            private String dateOfBirth;
            private String sex;
            private String nationality;
            private String ethnicity;
            private String religion;
            private String placeOfOrigin;
            private String placeOfResidence;
            private String personalIdentification;
            private String dateOfIssue;
            private String dateOfExpiry;
            private String fatherName;
            private String motherName;
            private String spouseName;
            private String oldCardNumber;

        }
    }
}
