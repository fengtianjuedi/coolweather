package com.wufeng.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wufeng.coolweather.gson.DailyForecast;
import com.wufeng.coolweather.gson.LifeStyle;
import com.wufeng.coolweather.gson.Weather;
import com.wufeng.coolweather.gson.WeatherAQI;
import com.wufeng.coolweather.gson.WeatherForecast;
import com.wufeng.coolweather.gson.WeatherLifeStyle;
import com.wufeng.coolweather.gson.WeatherNow;
import com.wufeng.coolweather.util.HttpUtil;
import com.wufeng.coolweather.util.Utility;

import java.io.IOException;
import java.util.prefs.Preferences;
import java.util.zip.Inflater;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degree;
    private TextView weatherInfo;
    private LinearLayout forecastLayout;
    private TextView aqi;
    private TextView pm25;
    private TextView comfort;
    private TextView carWash;
    private TextView sport;
    private ImageView bingIV;
    private Button homeBT;

    public SwipeRefreshLayout swipeRefreshLayout;
    public DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_weather);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degree = findViewById(R.id.degreeTV);
        weatherInfo = findViewById(R.id.weatherInfoTV);
        forecastLayout = findViewById(R.id.forecastLayout);
        aqi = findViewById(R.id.aqiTV);
        pm25 = findViewById(R.id.pm25TV);
        comfort = findViewById(R.id.comportTV);
        carWash = findViewById(R.id.carWashTV);
        sport = findViewById(R.id.sportTV);
        bingIV = findViewById(R.id.bingIV);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = findViewById(R.id.drawerLayout);
        homeBT = findViewById(R.id.homeBT);
        homeBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherNowString = sharedPreferences.getString("weatherNow", null);
        String weatherLifeStyleString = sharedPreferences.getString("weatherLifeStyle", null);
        String weatherForecastString = sharedPreferences.getString("weatherForecast", null);
        String weatherAQIString = sharedPreferences.getString("weatherAQI", null);
        String bingImage = sharedPreferences.getString("bingImage", null);
        final String weatherId = getIntent().getStringExtra("weather_id");
        if (bingImage != null){
            Glide.with(this).load(bingImage).into(bingIV);
        }else{
            loadBackgroundImage();
        }
        if (weatherNowString != null){
            WeatherNow weatherNow = Utility.handleWeatherNowResponse(weatherNowString);
            showWeatherNow(weatherNow);
        }else{
           requestWeatherNow(weatherId);
        }
        if (weatherLifeStyleString != null){
            WeatherLifeStyle weatherLifeStyle = Utility.handleWeatherLifeStyleResponse(weatherLifeStyleString);
            showWeatherLifeStyle(weatherLifeStyle);
        }else{
            requestWeatherLifeStyle(weatherId);
        }
        if (weatherForecastString != null){
            WeatherForecast weatherForecast = Utility.handleWeatherForecastResponse(weatherForecastString);
            showWeatherForecast(weatherForecast);
        }else{
            requestWeatherForecast(weatherId);
        }
        if (weatherAQIString != null){
            WeatherAQI weatherAQI = Utility.handleWeatherAQIResponse(weatherAQIString);
            showWeatherAQI(weatherAQI);
        }else {
            requestWeatherAQI(weatherId);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeatherNow(weatherId);
                requestWeatherForecast(weatherId);
                requestWeatherAQI(weatherId);
                requestWeatherLifeStyle(weatherId);
            }
        });
    }

    public void requestWeatherNow(final String weatherId){
        String url = "https://free-api.heweather.net/s6/weather/now?location=" + weatherId + "&key=2989582eaab64d21b1305a9a78c8915a";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final  String responseText = response.body().string();
                final WeatherNow weatherNow = Utility.handleWeatherNowResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weatherNow != null && "ok".equals(weatherNow.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weatherNow", responseText);
                            editor.apply();
                            showWeatherNow(weatherNow);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        loadBackgroundImage();
    }

    public void requestWeatherLifeStyle(final String weatherId){
        String url = "https://free-api.heweather.net/s6/weather/lifestyle?location=" + weatherId + "&key=2989582eaab64d21b1305a9a78c8915a";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final  String responseText = response.body().string();
                final WeatherLifeStyle weatherLifeStyle = Utility.handleWeatherLifeStyleResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weatherLifeStyle != null && "ok".equals(weatherLifeStyle.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weatherLifeStyle", responseText);
                            editor.apply();
                            showWeatherLifeStyle(weatherLifeStyle);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    public void requestWeatherForecast(final String weatherId){
        String url = "https://free-api.heweather.net/s6/weather/forecast?location=" + weatherId + "&key=2989582eaab64d21b1305a9a78c8915a";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final  String responseText = response.body().string();
                final WeatherForecast weatherForecast = Utility.handleWeatherForecastResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weatherForecast != null && "ok".equals(weatherForecast.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weatherForecast", responseText);
                            editor.apply();
                            showWeatherForecast(weatherForecast);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    public void requestWeatherAQI(final String weatherId){
        String url = "https://free-api.heweather.net/s6/air/now?location=" + weatherId + "&key=2989582eaab64d21b1305a9a78c8915a";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final  String responseText = response.body().string();
                final WeatherAQI weatherAQI = Utility.handleWeatherAQIResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weatherAQI != null && "ok".equals(weatherAQI.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weatherAQI", responseText);
                            editor.apply();
                            showWeatherAQI(weatherAQI);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void showWeatherNow(WeatherNow weatherNow){
        if (weatherNow == null)
            return;
        titleCity.setText(weatherNow.basic.cityName);
        titleUpdateTime.setText(weatherNow.update.updateTime.split(" ")[1]);
        degree.setText(weatherNow.now.temperature + "℃");
        weatherInfo.setText(weatherNow.now.info);
    }

    private void showWeatherForecast(WeatherForecast weatherForecast){
        if (weatherForecast == null)
            return;
        forecastLayout.removeAllViews();
        for (DailyForecast item : weatherForecast.dailyForecastList){
            View view = LayoutInflater.from(WeatherActivity.this).inflate(R.layout.forecast_item, null, false);
            TextView date = view.findViewById(R.id.dateTV);
            TextView info = view.findViewById(R.id.weatherInfoDateTV);
            TextView max = view.findViewById(R.id.temperatureMaxTV);
            TextView min = view.findViewById(R.id.temperatureMinTV);
            date.setText(item.date);
            info.setText(item.weatherConditionDate);
            max.setText(item.temperatureMax);
            min.setText(item.temperatureMin);
            forecastLayout.addView(view);
        }
    }

    private void showWeatherAQI(WeatherAQI weatherAQI){
        if (weatherAQI == null)
            return;
        aqi.setText(weatherAQI.aqi.aqi);
        pm25.setText(weatherAQI.aqi.pm25);
    }

    private void showWeatherLifeStyle(WeatherLifeStyle weatherLifeStyle){
        if (weatherLifeStyle == null)
            return;
        for (LifeStyle item : weatherLifeStyle.lifeStyleList){
            if ("comf".equals(item.type)){
                comfort.setText("舒适度：" + item.info);
            }else if ("cw".equals(item.type)){
                carWash.setText("洗车指数：" + item.info);
            }else if ("sport".equals(item.type)){
                sport.setText("运动指数：" + item.info);
            }
        }
    }

    private void loadBackgroundImage(){
        String url = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseString = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bingImage", responseString);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(responseString).into(bingIV);
                    }
                });
            }
        });
    }
}
