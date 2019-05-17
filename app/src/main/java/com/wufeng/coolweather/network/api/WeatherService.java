package com.wufeng.coolweather.network.api;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    @GET("s6/weather/now")
    Observable<String> getWeatherNow(@Query("location")String location, @Query("key")String key);

    @GET("s6/weather/lifestyle")
    Observable<String> getWeatherLifestyle(@Query("location")String location, @Query("key")String key);

    @GET("s6/weather/forecast")
    Observable<String> getWeatherForecast(@Query("location")String location, @Query("key")String key);

    @GET("s6/air/now")
    Observable<String> getWeatherAQI(@Query("location")String location, @Query("key")String key);
}
