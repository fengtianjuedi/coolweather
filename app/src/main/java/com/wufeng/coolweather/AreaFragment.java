package com.wufeng.coolweather;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wufeng.coolweather.db.City;
import com.wufeng.coolweather.db.County;
import com.wufeng.coolweather.db.Province;
import com.wufeng.coolweather.network.ServiceCreator;
import com.wufeng.coolweather.util.Utility;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class AreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressBar progressBar;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;
    private Disposable provincesDisposable;
    private Disposable cityDisposable;
    private Disposable countyDisposable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_area, container, false);
        titleText = view.findViewById(R.id.titleTV);
        backButton = view.findViewById(R.id.backBT);
        listView = view.findViewById(R.id.areaLS);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCity();
                }else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounty();
                }else if (currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                    editor.putString("weather_id", weatherId);
                    editor.apply();
                    if (getActivity() instanceof MainActivity){
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity() instanceof WeatherActivity){
                        WeatherActivity weatherActivity = (WeatherActivity)getActivity();
                        weatherActivity.drawerLayout.closeDrawers();
                        weatherActivity.swipeRefreshLayout.setRefreshing(true);
                        weatherActivity.requestWeatherNow(weatherId);
                        weatherActivity.requestWeatherForecast(weatherId);
                        weatherActivity.requestWeatherAQI(weatherId);
                        weatherActivity.requestWeatherLifeStyle(weatherId);
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY){
                    queryCity();
                }else if (currentLevel == LEVEL_CITY){
                    queryProvince();
                }
            }
        });
        queryProvince();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (provincesDisposable != null &&!provincesDisposable.isDisposed())
            provincesDisposable.dispose();
        if (cityDisposable != null && !cityDisposable.isDisposed())
            cityDisposable.dispose();
        if (countyDisposable != null && !countyDisposable.isDisposed())
            countyDisposable.dispose();
    }

    private void queryProvince(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class);
        if (provinceList.size() > 0){
            dataList.clear();
            for (Province item : provinceList){
                dataList.add(item.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            requestProvinces();
        }
    }

    private void queryCity(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = LitePal.where("provinceId = ?", String.valueOf(selectedProvince.getProvinceCode())).find(City.class);
        if (cityList.size() > 0){
            dataList.clear();
            for (City item : cityList){
                dataList.add(item.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            requestCities(provinceCode);
        }
    }

    private void queryCounty(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityId = ?", String.valueOf(selectedCity.getCityCode())).find(County.class);
        if (countyList.size() > 0){
            dataList.clear();
            for (County item : countyList){
                dataList.add(item.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            requestCounties(provinceCode, cityCode);
        }
    }

    private void requestProvinces(){
        showProgressBar();
        provincesDisposable = ServiceCreator.placeService.getProvinces()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        closeProgressBar();
                        boolean result;
                        result = Utility.handleProvinceResponse(s);
                        if (result) {
                            queryProvince();
                        }
                        if (!provincesDisposable.isDisposed())
                            provincesDisposable.dispose();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        closeProgressBar();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                        if (!provincesDisposable.isDisposed())
                            provincesDisposable.dispose();
                    }
                });
    }

    private void requestCities(final int provinceId){
        showProgressBar();
        cityDisposable = ServiceCreator.placeService.getCities(provinceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        closeProgressBar();
                        boolean result;
                        result = Utility.handleCityResponse(s, provinceId);
                        if (result){
                            queryCity();
                        }
                        if (!cityDisposable.isDisposed())
                            cityDisposable.dispose();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        closeProgressBar();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                        if (!cityDisposable.isDisposed())
                            cityDisposable.dispose();
                    }
                });
    }

    private void requestCounties(final int provinceId, final int cityId){
        showProgressBar();
        countyDisposable = ServiceCreator.placeService.getCounties(provinceId, cityId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        closeProgressBar();
                        boolean result;
                        result = Utility.handleCountyResponse(s, cityId);
                        if (result){
                            queryCounty();
                        }
                        if (!countyDisposable.isDisposed())
                            countyDisposable.dispose();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        closeProgressBar();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                        if (!countyDisposable.isDisposed())
                            countyDisposable.dispose();
                    }
                });
    }

    private void showProgressBar(){
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        if (progressBar == null){
            progressBar = new ProgressBar(getContext());
        }
        progressBar.setVisibility(View.VISIBLE);
    }

    private void closeProgressBar(){
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        if (progressBar != null){
            progressBar.setVisibility(View.GONE);
        }
    }
}
