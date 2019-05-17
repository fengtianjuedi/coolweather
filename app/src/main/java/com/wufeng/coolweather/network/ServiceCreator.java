package com.wufeng.coolweather.network;

import com.wufeng.coolweather.network.api.PlaceService;
import com.wufeng.coolweather.network.api.WeatherService;

import io.reactivex.internal.schedulers.RxThreadFactory;
import io.reactivex.plugins.RxJavaPlugins;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ServiceCreator {
    public static PlaceService placeService;
    public static WeatherService weatherService;

    static {
        Retrofit retrofitPlace = new Retrofit.Builder()
                .baseUrl("http://guolin.tech/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        placeService = retrofitPlace.create(PlaceService.class);

        Retrofit retrofitWeather = new Retrofit.Builder()
                .baseUrl("https://free-api.heweather.net/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        weatherService = retrofitWeather.create(WeatherService.class);
    }
}
