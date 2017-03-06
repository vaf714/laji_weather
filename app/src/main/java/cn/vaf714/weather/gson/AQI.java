package cn.vaf714.weather.gson;

/**
 * Created by Passerby_B on 2017/3/4.
 */

public class AQI {
    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
