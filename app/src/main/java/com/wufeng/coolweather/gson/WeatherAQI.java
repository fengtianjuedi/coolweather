package com.wufeng.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherAQI extends Weather {
    @SerializedName("air_now_city")
    public AQI aqi;
}
