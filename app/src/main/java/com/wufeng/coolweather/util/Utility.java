package com.wufeng.coolweather.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.wufeng.coolweather.db.City;
import com.wufeng.coolweather.db.County;
import com.wufeng.coolweather.db.Province;
import com.wufeng.coolweather.gson.Weather;
import com.wufeng.coolweather.gson.WeatherAQI;
import com.wufeng.coolweather.gson.WeatherForecast;
import com.wufeng.coolweather.gson.WeatherLifeStyle;
import com.wufeng.coolweather.gson.WeatherNow;

import java.io.SyncFailedException;
import java.lang.reflect.Type;

public class Utility {
    public static boolean handleProvinceResponse(String response){
        if (TextUtils.isEmpty(response))
            return false;
        Gson gson = new Gson();
        JsonArray allProvinces = gson.fromJson(response, JsonArray.class);
        for (int i = 0; i < allProvinces.size(); i++){
            JsonObject jsonObject = allProvinces.get(i).getAsJsonObject();
            Province province = new Province();
            province.setProvinceName(jsonObject.get("name").getAsString());
            province.setProvinceCode(jsonObject.get("id").getAsInt());
            province.save();
        }
        return true;
    }

    public static boolean handleCityResponse(String response, int provinceId){
        if (TextUtils.isEmpty(response))
            return false;
        Gson gson = new Gson();
        JsonArray allCity = gson.fromJson(response, JsonArray.class);
        for (int i = 0; i < allCity.size(); i++){
            JsonObject jsonObject = allCity.get(i).getAsJsonObject();
            City city = new City();
            city.setCityName(jsonObject.get("name").getAsString());
            city.setCityCode(jsonObject.get("id").getAsInt());
            city.setProvinceId(provinceId);
            city.save();
        }
        return true;
    }

    public static boolean handleCountyResponse(String response, int cityId){
        if (TextUtils.isEmpty(response))
            return false;
        Gson gson = new Gson();
        JsonArray allCounty = gson.fromJson(response, JsonArray.class);
        for (int i = 0; i < allCounty.size(); i++){
            JsonObject jsonObject = allCounty.get(i).getAsJsonObject();
            County county = new County();
            county.setCountyName(jsonObject.get("name").getAsString());
            county.setWeatherId(jsonObject.get("weather_id").getAsString());
            county.setCityId(cityId);
            county.save();
        }
        return true;
    }

    public static <T extends Weather> T handleWeatherResponse(String response, Class<T> type){
        if (TextUtils.isEmpty(response))
            return null;
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
        JsonArray jsonArray = gson.fromJson(jsonObject.get("HeWeather6"), JsonArray.class);
        return gson.fromJson(jsonArray.get(0), type);
    }

}
