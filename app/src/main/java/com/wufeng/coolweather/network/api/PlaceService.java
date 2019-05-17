package com.wufeng.coolweather.network.api;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PlaceService {
    @GET("api/china")
    Observable<String> getProvinces();

    @GET("api/china/{provinceId}")
    Observable<String> getCities(@Path("provinceId") int provinceId);

    @GET("api/china/{provinceId}/{cityId}")
    Observable<String> getCounties(@Path("provinceId")int provinceId, @Path("cityId")int cityId);

    @GET("api/bing_pic")
    Observable<String> getBingImage();
}
