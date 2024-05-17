package com.umut.soysal.modal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class WebSocketModal {
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OnSuccessFormat {
        private Instant timestamp;
        private Success success;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Getter
        @Setter
        public static class Success {
            private PersonalData personalData;
            private Validation validation;
            private Session session;

            @JsonIgnoreProperties(ignoreUnknown = true)
            @Getter
            @Setter
            @NoArgsConstructor
            @AllArgsConstructor
            public static class PersonalData {
                private String mrz;
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
                private String cardFontImage;
                private String cardBackImage;
                private String originalImage;
                private String capturedImage;

                public static PersonalData from(ValidationModal.ValidationResponse.PersonalData data, String capturedImage) {
                    return from(data, capturedImage, null, null);
                }

                public static PersonalData from(ValidationModal.ValidationResponse.PersonalData data, String capturedImage, String cardFontImage, String cardBackImage) {
                    return new WebSocketModal.OnSuccessFormat.Success.PersonalData(
                            data.getMrz(), data.getCardNumber(), data.getFullName(),
                            data.getDateOfBirth(), data.getSex(), data.getNationality(), data.getEthnicity(),
                            data.getReligion(), data.getPlaceOfOrigin(), data.getPlaceOfResidence(),
                            data.getPersonalIdentification(), data.getDateOfIssue(), data.getDateOfExpiry(), data.getFatherName(),
                            data.getMotherName(), data.getSpouseName(), data.getOldCardNumber(), cardFontImage,
                            cardBackImage, data.getFaceImage(), capturedImage
                    );
                }

                public static PersonalData from(ValidationModal.ValidationResponse.PersonalData data) {
                    return from(data, null);
                }
            }


            @JsonIgnoreProperties(ignoreUnknown = true)
            @Getter
            @Setter
            public static class Validation {
                private String id;
                private Instant createdDate;
                private Boolean cardIntegrityResult;
                private String cardIntegrityMessage;
                private Boolean cardLegitimacyResult;
                private String cardLegitimacyMessage;
                private Boolean faceCompareResult;
                private String faceCompareMessage;
                private Float faceCompareAccuracy;
                private Boolean faceLivenessResult;
                private String faceLivenessMessage;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            @Getter
            @Setter
            public static class Session {
                private String clientName;
                private String deviceName;
                private String deviceSerial;
            }
        }
    }

    @Getter
    @Setter
    public static class OnErrorFormat {
        private Instant createdDate;
        private Error error;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Getter
        @Setter
        public static class Error {
            private Boolean status;
            private String message;
        }
    }


}
