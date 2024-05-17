package com.umut.soysal.modal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class AuthClientModal {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String account;
        private String password;
        private Boolean remember;
        private String clientName;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class LoginResponse {
        private Instant timestamp;
        private int status;
        private String error;
        private String message;
        private Response response;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class Response {
        private String clientId;
        private String token;
        private String id;
        private String subject;
        private String role;
        private String type;
        private String issuer;
        private Instant expiresAt;
        private String clientName;
        private String platform;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class LogoutResponse {
        private Instant timestamp;
        private int status;
        private String error;
        private String message;
        private Response response;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenRequest {
        private String account;
        private String password;
        private Boolean remember;
    }
}
