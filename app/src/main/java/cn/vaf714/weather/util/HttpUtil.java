package cn.vaf714.weather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Passerby_B on 2017/3/4.
 */

public class HttpUtil {
    /**
     * 发送http请求
     * @param address 请求地址
     * @param callback 回调对象,封装回调函数
     */
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
