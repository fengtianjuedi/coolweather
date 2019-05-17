package com.wufeng.coolweather.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.wufeng.coolweather.R;
import com.wufeng.coolweather.WeatherActivity;
import com.wufeng.coolweather.gson.WeatherAQI;
import com.wufeng.coolweather.gson.WeatherForecast;
import com.wufeng.coolweather.gson.WeatherLifeStyle;
import com.wufeng.coolweather.gson.WeatherNow;
import com.wufeng.coolweather.network.ServiceCreator;
import com.wufeng.coolweather.util.Utility;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class AutoUpdateService extends Service {
    private Disposable nowDisposable;
    private Disposable lifestyleDisposable;
    private Disposable forecastDisposable;
    private Disposable aqiDisposable;
    private Disposable bingImageDisposable;

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

    @Override
    public void onDestroy() {
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
        nowDisposable = ServiceCreator.weatherService.getWeatherNow(weatherId, "2989582eaab64d21b1305a9a78c8915a")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        WeatherNow weatherNow = Utility.handleWeatherResponse(s, WeatherNow.class);
                        if (weatherNow != null && "ok".equals(weatherNow.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                            editor.putString("weatherNow", s);
                            editor.apply();
                            pushNotification();
                        }
                        if (!nowDisposable.isDisposed())
                            nowDisposable.dispose();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        if (!nowDisposable.isDisposed())
                            nowDisposable.dispose();
                    }
                });
    }

    public void requestWeatherLifeStyle(final String weatherId){
        lifestyleDisposable = ServiceCreator.weatherService.getWeatherLifestyle(weatherId, "2989582eaab64d21b1305a9a78c8915a")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        WeatherLifeStyle weatherLifeStyle = Utility.handleWeatherResponse(s, WeatherLifeStyle.class);
                        if (weatherLifeStyle != null && "ok".equals(weatherLifeStyle.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                            editor.putString("weatherLifeStyle", s);
                            editor.apply();
                        }
                        if (!lifestyleDisposable.isDisposed())
                            lifestyleDisposable.dispose();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
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
                    public void accept(String s) throws Exception {
                        WeatherForecast weatherForecast = Utility.handleWeatherResponse(s, WeatherForecast.class);
                        if (weatherForecast != null && "ok".equals(weatherForecast.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                            editor.putString("weatherForecast", s);
                            editor.apply();
                        }
                        if (!forecastDisposable.isDisposed())
                            forecastDisposable.dispose();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
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
                    public void accept(String s) throws Exception {
                        WeatherAQI weatherAQI = Utility.handleWeatherResponse(s, WeatherAQI.class);
                        if (weatherAQI != null && "ok".equals(weatherAQI.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                            editor.putString("weatherAQI", s);
                            editor.apply();
                        }
                        if (!aqiDisposable.isDisposed())
                            aqiDisposable.dispose();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        if (!aqiDisposable.isDisposed())
                            aqiDisposable.dispose();
                    }
                });
    }

    private void updateBindImage(){
        bingImageDisposable = ServiceCreator.placeService.getBingImage()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("bingImage", s);
                        editor.apply();
                        if (!bingImageDisposable.isDisposed())
                            bingImageDisposable.dispose();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        if (!bingImageDisposable.isDisposed())
                            bingImageDisposable.dispose();
                    }
                });
    }

    private void pushNotification(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherId = sharedPreferences.getString("weather_id", null);
        Intent intent = new Intent(this, WeatherActivity.class);
        intent.putExtra("weather_id", weatherId);
        PendingIntent pi = PendingIntent.getService(this, 0, intent, 0);
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setContentTitle("你有新的天气信息！")
                .setContentText("天气变化，注意-----")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.clound)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.clound))
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build();
        manager.notify(1, notification);
    }
}
