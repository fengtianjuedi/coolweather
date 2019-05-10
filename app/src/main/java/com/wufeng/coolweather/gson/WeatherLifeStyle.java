package com.wufeng.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherLifeStyle extends Weather {
    @SerializedName("lifestyle")
    public List<LifeStyle>  lifeStyleList;
}
