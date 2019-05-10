package com.wufeng.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherForecast extends Weather {
    @SerializedName("daily_forecast")
    public List<DailyForecast> dailyForecastList;
}
