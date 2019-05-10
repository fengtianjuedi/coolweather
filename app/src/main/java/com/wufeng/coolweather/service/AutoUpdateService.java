package com.wufeng.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wufeng.coolweather.WeatherActivity;
import com.wufeng.coolweather.gson.WeatherAQI;
import com.wufeng.coolweather.gson.WeatherForecast;
import com.wufeng.coolweather.gson.WeatherLifeStyle;
import com.wufeng.coolweather.gson.WeatherNow;
import com.wufeng.coolweather.util.HttpUtil;
import com.wufeng.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBindImage();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherId = sharedPreferences.getString("weather_id", null);
        if (weatherId != null){
            requestWeatherNow(weatherId);
            requestWeatherAQI(weatherId);
            requestWeatherForecast(weatherId);
            requestWeatherLifeStyle(weatherId);
        }
    }

    public void requestWeatherNow(final String weatherId){
        String url = "https://free-api.heweather.net/s6/weather/now?location=" + weatherId + "&key=2989582eaab64d21b1305a9a78c8915a";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final  String responseText = response.body().string();
                final WeatherNow weatherNow = Utility.handleWeatherNowResponse(responseText);
                if (weatherNow != null && "ok".equals(weatherNow.status)){
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                    editor.putString("weatherNow", responseText);
                    editor.apply();
                }
            }
        });
    }

    public void requestWeatherLifeStyle(final String weatherId){
        String url = "https://free-api.heweather.net/s6/weather/lifestyle?location=" + weatherId + "&key=2989582eaab64d21b1305a9a78c8915a";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final  String responseText = response.body().string();
                final WeatherLifeStyle weatherLifeStyle = Utility.handleWeatherLifeStyleResponse(responseText);
                if (weatherLifeStyle != null && "ok".equals(weatherLifeStyle.status)){
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                    editor.putString("weatherLifeStyle", responseText);
                    editor.apply();
                }
            }
        });
    }

    public void requestWeatherForecast(final String weatherId){
        String url = "https://free-api.heweather.net/s6/weather/forecast?location=" + weatherId + "&key=2989582eaab64d21b1305a9a78c8915a";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final  String responseText = response.body().string();
                final WeatherForecast weatherForecast = Utility.handleWeatherForecastResponse(responseText);
                if (weatherForecast != null && "ok".equals(weatherForecast.status)){
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                    editor.putString("weatherForecast", responseText);
                    editor.apply();
                }
            }
        });
    }

    public void requestWeatherAQI(final String weatherId){
        String url = "https://free-api.heweather.net/s6/air/now?location=" + weatherId + "&key=2989582eaab64d21b1305a9a78c8915a";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final WeatherAQI weatherAQI = Utility.handleWeatherAQIResponse(responseText);
                if (weatherAQI != null && "ok".equals(weatherAQI.status)) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                    editor.putString("weatherAQI", responseText);
                    editor.apply();
                }
            }
        });
    }

    private void updateBindImage(){
        String url = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseString = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bingImage", responseString);
                editor.apply();
            }
        });
    }
}
