package com.wufeng.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class AQI {
    public String aqi;
    public String pm25;
    @SerializedName("qlty")
    public String airQuality;
}
