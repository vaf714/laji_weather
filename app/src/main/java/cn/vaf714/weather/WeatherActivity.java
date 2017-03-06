package cn.vaf714.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.io.IOException;

import cn.vaf714.weather.gson.Forecast;
import cn.vaf714.weather.gson.Weather;
import cn.vaf714.weather.service.AutoUpdateService;
import cn.vaf714.weather.util.HttpUtil;
import cn.vaf714.weather.util.Utility;
import jp.wasabeef.glide.transformations.BlurTransformation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    //必应背景图片
    private ImageView bingPicImg;
    //侧滑菜单
    public DrawerLayout drawerLayout;
    //下拉刷新
    public SwipeRefreshLayout swipeRefreshLayout;
    //标题栏
    private TextView titleCity;//标题
    private TextView titleUpdateTime;//更新时间
    private Button navButton;//打开侧滑菜单
    //当前天气
    private TextView degreeText;//温度
    private TextView weatherInfoText;//天气类型
    //预报
    private LinearLayout forecastLayout;
    //空气质量
    private TextView aqiText;//aqi
    private TextView pm25Text;//pm2.5
    //生活建议
    private TextView comfortText;//舒适度
    private TextView carWashText;//洗车指数
    private TextView sportText;//运动建议

    private String mWeatherId;//记住当前weather的ID，用于下拉刷新的请求id
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置沉浸状态栏
       if (Build.VERSION.SDK_INT >= 21){
            //android5.0 以上执行
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);//状态栏透明
        }

        setContentView(R.layout.activity_weather);
        //初始化控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        navButton = (Button) findViewById(R.id.nav_button);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);//设置下拉刷新颜色
        //监听打开侧滑按钮
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        //读取缓存
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather", null);
        String bingPic = preferences.getString("bing_pic",null);
        //判断是否有背景图片缓存
        if (bingPic != null){
            //存在缓存
            Glide.with(this).load(bingPic).bitmapTransform(new BlurTransformation(getApplicationContext(),50)).into(bingPicImg);
        }else{
            //没有缓存，请求加载缓存
            loadBingPic();
        }
        //判断缓存是否有数据
        if (weatherString != null) {
            //有缓存时，读取缓存
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;//记录当前城市
            showWeatherInfo(weather);
        } else {
            //无缓存时，服务器查询
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);//隐藏界面
            requestWeather(mWeatherId);
        }
        //后台更新
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
    }

    /**
     * 加载必应图片
     */
    private void loadBingPic() {
        String address = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 根据id获取天气信息
     *
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {
        String key = "398fb28a421f4764aaaf8e3fa91dec72";
        String address = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=" + key;
        Log.d("WeatherActivity_Address", address);
        //发送请求
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //解析响应数据成Weather
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather.status.equals("ok") && weather != null) {
                            //主线程添加缓存
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            //缓存成功后，更改当前weatherId
                            mWeatherId = weatherId;
                            //主线程中调用显示函数
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);//关闭刷新
                    }
                });
            }
        });
    }

    /**
     * 处理展示Weather数据
     *
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime;
        updateTime = updateTime.substring(updateTime.lastIndexOf(" "));
        String degree = weather.now.temperature;
        String weatherInfo = weather.now.more.info;
        String comfort = "舒适度 : " + weather.suggestion.comfort.info;
        String carWash = "洗车指数 : " + weather.suggestion.carWash.info;
        String sport = "运动建议 : " + weather.suggestion.sport.info;
        //设置标题栏
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        //设置当前天气
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        //设置预报
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecasts) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max + "°");
            minText.setText(forecast.temperature.min + "°");
            forecastLayout.addView(view);
        }
        //设置空气质量
        if (weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        //设置生活建议
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        //显示控件
        weatherLayout.setVisibility(View.VISIBLE);
    }
}
