package com.umut.soysal.modal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InformationModal {
    @SerializedName("timestamp")
    private Instant timestamp;

    @SerializedName("status")
    private int status;

    @SerializedName("response")
    private ResponseData response;

    @SerializedName("error")
    private String error;

    @SerializedName("message")
    private String message;

    @Getter
    @Setter
    public static class ResponseData {
        @SerializedName("profile")
        private ProfileData profile;

        @SerializedName("quota")
        private QuotaData quota;
    }

    @Getter
    @Setter
    public static class ProfileData {
        @SerializedName("id")
        private String id;

        @SerializedName("login")
        private String login;

        @SerializedName("email")
        private String email;

        @SerializedName("name")
        private String name;

        @SerializedName("imageUrl")
        private String imageUrl;

        @SerializedName("imageBase64")
        private String imageBase64;

        @SerializedName("createdDate")
        private Instant createdDate;

        @SerializedName("lastModifiedDate")
        private Instant lastModifiedDate;

        @SerializedName("status")
        private String status;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class QuotaData {
        @SerializedName("accountId")
        private UUID accountId;

        @SerializedName("quotaIntegrity")
        private QuotaDetail quotaIntegrity;

        @SerializedName("quotaLegitimacy")
        private QuotaDetail quotaLegitimacy;

        @SerializedName("quotaFaceCompare")
        private QuotaDetail quotaFaceCompare;

        @SerializedName("quotaFaceLiveness")
        private QuotaDetail quotaFaceLiveness;

        @SerializedName("quotaFileExport")
        private QuotaDetail quotaFileExport;

        @SerializedName("quotaUser")
        private QuotaDetail quotaUser;

        @SerializedName("expiration")
        private Instant expiration;
    }

    @Getter
    @Setter
    public static class QuotaDetail {
        @SerializedName("total")
        private int total;

        @SerializedName("used")
        private int used;

        @SerializedName("expired")
        private int expired;

        @SerializedName("remain")
        private int remain;
    }
}
