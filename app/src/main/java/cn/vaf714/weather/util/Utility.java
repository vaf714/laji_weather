package cn.vaf714.weather.util;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.vaf714.weather.db.City;
import cn.vaf714.weather.db.County;
import cn.vaf714.weather.db.Province;
import cn.vaf714.weather.gson.Weather;

/**
 * Created by Passerby_B on 2017/3/4.
 */

public class Utility {
    /**
     * 解析服务器返回的json省级数据
     *
     * @param response 服务器返回的json数据
     * @return
     */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray provinces = new JSONArray(response);
                for (int i = 0; i < provinces.length(); i++) {
                    JSONObject jsonObject = provinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(jsonObject.getString("name"));
                    province.setProvinceCode(jsonObject.getInt("id"));
                    province.save();
                    Log.d("Utility_province" + (i + 1), province.toString());
                }
                return true;//解析成功
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;//解析失败
    }

    /**
     * 解析服务器返回的json市级数据
     *
     * @param provinceId 归属省份
     * @param response   服务器返回的json数据
     * @return
     */
    public static boolean handleCityResponse(int provinceId, String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray provinces = new JSONArray(response);
                for (int i = 0; i < provinces.length(); i++) {
                    JSONObject jsonObject = provinces.getJSONObject(i);
                    City city = new City();
                    city.setProvinceId(provinceId);
                    city.setCityName(jsonObject.getString("name"));
                    city.setCityCode(jsonObject.getInt("id"));
                    city.save();
                    Log.d("Utility_city" + (i + 1), city.toString());
                }
                return true;//解析成功
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;//解析失败
    }

    /**
     * 解析服务器返回的json县级数据
     *
     * @param cityId   归属市
     * @param response 服务器返回的json数据
     * @return
     */
    public static boolean handleCountyResponse(int cityId, String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray provinces = new JSONArray(response);
                for (int i = 0; i < provinces.length(); i++) {
                    JSONObject jsonObject = provinces.getJSONObject(i);
                    County county = new County();
                    county.setCityId(cityId);
                    county.setCountyName(jsonObject.getString("name"));
                    county.setWeatherId(jsonObject.getString("weather_id"));
                    county.save();
                    Log.d("Utility_county" + (i + 1), county.toString());
                }
                return true;//解析成功
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;//解析失败
    }

    /**
     * 将请求的json解析成Weather实体类
     * @param response
     * @return
     */
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
