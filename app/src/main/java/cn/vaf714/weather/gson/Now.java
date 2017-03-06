package cn.vaf714.weather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Passerby_B on 2017/3/4.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;
    }
}
