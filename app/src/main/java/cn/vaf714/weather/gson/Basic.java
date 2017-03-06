package cn.vaf714.weather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Passerby_B on 2017/3/4.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
