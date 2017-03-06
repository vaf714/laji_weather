package cn.vaf714.weather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Passerby_B on 2017/3/4.
 */

public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;
    @SerializedName("daily_forecast")
    public List<Forecast> forecasts;
}
