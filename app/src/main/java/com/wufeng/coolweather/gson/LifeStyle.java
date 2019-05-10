package com.wufeng.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class LifeStyle {
    public String type;
    @SerializedName("brf")
    public String resume;
    @SerializedName("txt")
    public String info;
}
