package com.wufeng.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wufeng.coolweather.gson.DailyForecast;
import com.wufeng.coolweather.gson.LifeStyle;
import com.wufeng.coolweather.gson.WeatherAQI;
import com.wufeng.coolweather.gson.WeatherForecast;
import com.wufeng.coolweather.gson.WeatherLifeStyle;
import com.wufeng.coolweather.gson.WeatherNow;
import com.wufeng.coolweather.network.ServiceCreator;
import com.wufeng.coolweather.service.AutoUpdateService;
import com.wufeng.coolweather.util.Utility;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

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
    private Disposable nowDisposable;
    private Disposable lifestyleDisposable;
    private Disposable forecastDisposable;
    private Disposable aqiDisposable;
    private Disposable bingImageDisposable;

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
            WeatherNow weatherNow = Utility.handleWeatherResponse(weatherNowString, WeatherNow.class);
            showWeatherNow(weatherNow);
        }else{
           requestWeatherNow(weatherId);
        }
        if (weatherLifeStyleString != null){
            WeatherLifeStyle weatherLifeStyle = Utility.handleWeatherResponse(weatherLifeStyleString, WeatherLifeStyle.class);
            showWeatherLifeStyle(weatherLifeStyle);
        }else{
            requestWeatherLifeStyle(weatherId);
        }
        if (weatherForecastString != null){
            WeatherForecast weatherForecast = Utility.handleWeatherResponse(weatherForecastString, WeatherForecast.class);
            showWeatherForecast(weatherForecast);
        }else{
            requestWeatherForecast(weatherId);
        }
        if (weatherAQIString != null){
            WeatherAQI weatherAQI = Utility.handleWeatherResponse(weatherAQIString, WeatherAQI.class);
            showWeatherAQI(weatherAQI);
        }else {
            requestWeatherAQI(weatherId);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SharedPreferences sharedPreferences1 = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                String id = sharedPreferences1.getString("weather_id", null);
                if (id != null){
                    requestWeatherNow(id);
                    requestWeatherForecast(id);
                    requestWeatherAQI(id);
                    requestWeatherLifeStyle(id);
                }
            }
        });
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (nowDisposable != null && !nowDisposable.isDisposed())
            nowDisposable.dispose();
        if (lifestyleDisposable != null && !lifestyleDisposable.isDisposed())
            lifestyleDisposable.dispose();
        if (forecastDisposable != null && !forecastDisposable.isDisposed())
            forecastDisposable.dispose();
        if (aqiDisposable != null && !aqiDisposable.isDisposed())
            aqiDisposable.dispose();
        if (bingImageDisposable != null && !bingImageDisposable.isDisposed())
            bingImageDisposable.dispose();
    }

    public void requestWeatherNow(final String weatherId){
        nowDisposable = ServiceCreator.weatherService.getWeatherNow(weatherId, "2989582eaab64d21b1305a9a78c8915a")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s){
                        WeatherNow weatherNow = Utility.handleWeatherResponse(s, WeatherNow.class);
                        if (weatherNow != null && "ok".equals(weatherNow.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weatherNow", s);
                            editor.apply();
                            showWeatherNow(weatherNow);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        loadBackgroundImage();
                        if (!nowDisposable.isDisposed())
                            nowDisposable.dispose();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable){
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        if (!nowDisposable.isDisposed())
                            nowDisposable.dispose();
                    }
                });
    }

    public void requestWeatherLifeStyle(final String weatherId){
        lifestyleDisposable =  ServiceCreator.weatherService.getWeatherLifestyle(weatherId, "2989582eaab64d21b1305a9a78c8915a")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s){
                        WeatherLifeStyle weatherLifeStyle = Utility.handleWeatherResponse(s, WeatherLifeStyle.class);
                        if (weatherLifeStyle != null && "ok".equals(weatherLifeStyle.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weatherLifeStyle", s);
                            editor.apply();
                            showWeatherLifeStyle(weatherLifeStyle);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                        if (!lifestyleDisposable.isDisposed())
                            lifestyleDisposable.dispose();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable){
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                        if (!lifestyleDisposable.isDisposed())
                            lifestyleDisposable.dispose();
                    }
                });
    }

    public void requestWeatherForecast(final String weatherId){
        forecastDisposable = ServiceCreator.weatherService.getWeatherForecast(weatherId, "2989582eaab64d21b1305a9a78c8915a")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s){
                        WeatherForecast weatherForecast = Utility.handleWeatherResponse(s, WeatherForecast.class);
                        if (weatherForecast != null && "ok".equals(weatherForecast.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weatherForecast", s);
                            editor.apply();
                            showWeatherForecast(weatherForecast);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        if (!forecastDisposable.isDisposed())
                            forecastDisposable.dispose();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable){
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        if (!forecastDisposable.isDisposed())
                            forecastDisposable.dispose();
                    }
                });
    }

    public void requestWeatherAQI(final String weatherId){
        aqiDisposable = ServiceCreator.weatherService.getWeatherAQI(weatherId, "2989582eaab64d21b1305a9a78c8915a")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s){
                        WeatherAQI weatherAQI = Utility.handleWeatherResponse(s, WeatherAQI.class);
                        if (weatherAQI != null && "ok".equals(weatherAQI.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weatherAQI", s);
                            editor.apply();
                            showWeatherAQI(weatherAQI);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        if (!aqiDisposable.isDisposed())
                            aqiDisposable.dispose();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable){
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        if (!aqiDisposable.isDisposed())
                            aqiDisposable.dispose();
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
        bingImageDisposable = ServiceCreator.placeService.getBingImage()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("bingImage", s);
                        editor.apply();
                        Glide.with(WeatherActivity.this).load(s).into(bingIV);
                        if (!bingImageDisposable.isDisposed())
                            bingImageDisposable.dispose();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable){
                        throwable.printStackTrace();
                        if (!bingImageDisposable.isDisposed())
                            bingImageDisposable.dispose();
                    }
                });
    }
}
