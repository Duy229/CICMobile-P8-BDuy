package com.umut.soysal.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface HealthCheckService {
    @GET("/actuator/health")
    Call<ResponseBody> getHealth();
}
