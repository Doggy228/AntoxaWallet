package edu.doggy228.antoxawallet;

import com.fasterxml.jackson.databind.JsonNode;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface WalletService {
    @GET("wu")
    Call<JsonNode> walletUser(@Header("Authorization") String authorization);
    @POST("oper-pay-check")
    Call<JsonNode> operPayCheck(@Header("Authorization") String authorization,@Body ReqOperPayCheck req);

}

