package com.wufeng.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class DailyForecast {
    @SerializedName("cond_txt_d")
    public String weatherConditionDate;
    @SerializedName("cond_txt_n")
    public String weatherConditionNight;
    public String date;
    @SerializedName("tmp_max")
    public String temperatureMax;
    @SerializedName("tmp_min")
    public String temperatureMin;
}
