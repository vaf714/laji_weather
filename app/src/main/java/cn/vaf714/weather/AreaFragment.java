package cn.vaf714.weather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.vaf714.weather.db.City;
import cn.vaf714.weather.db.County;
import cn.vaf714.weather.db.Province;
import cn.vaf714.weather.util.HttpUtil;
import cn.vaf714.weather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Passerby_B on 2017/3/4.
 */

public class AreaFragment extends Fragment {
    private Button back_button;
    private TextView title_text;
    private ListView list_view;
    private ProgressDialog progressDialog;
    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();//要给适配器传入的列表集合
    private List<Province> provinces = new ArrayList<>();//查询到的省份列表集合
    private List<City> cities = new ArrayList<>();//查询到的城市列表集合
    private List<County> counties = new ArrayList<>();//查询到的县级列表集合

    private Province selectProvince;//当前选中省份
    private City selectCity;//当前选中城市

    private int currentLevel;//当前选中的级别
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        //加载碎片
        View view = inflater.inflate(R.layout.choose_area, container, false);

        //获取控件
        back_button = (Button) view.findViewById(R.id.back_button);
        title_text = (TextView) view.findViewById(R.id.title_text);
        list_view = (ListView) view.findViewById(R.id.list_view);

        //设置列表适配器
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        list_view.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //设置列表点击监听
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE){//当前列表是省份
                    selectProvince = provinces.get(position);
                    //查询城市
                    queryCity();
                }else if (currentLevel == LEVEL_CITY){//当前列表是城市
                    selectCity = cities.get(position);
                    //查询县
                    queryCounty();
                }else if (currentLevel == LEVEL_COUNTY){//当前列表是县
                    String weatherId = counties.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity){//第一次打开
                        //跳转到天气界面
                        Intent intent = new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity() instanceof WeatherActivity){//侧滑打开
                        WeatherActivity weatherActivity = (WeatherActivity) getActivity();
                        weatherActivity.drawerLayout.closeDrawers();//关闭侧滑

                        //刷新界面
                        weatherActivity.swipeRefreshLayout.setRefreshing(true);
                        weatherActivity.requestWeather(weatherId);
                    }

                }
            }
        });
        //设置返回按钮点击监听
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY){//当前列表为县级
                    //查询城市
                    queryCity();
                }else if (currentLevel == LEVEL_CITY) {//当前列表为城市
                    //查询省份
                    queryProvince();
                }
            }
        });
        //初始化时设为省份列表
        queryProvince();
    }

    /**
     * 查询省份
     */
    private void queryProvince(){
        //设置导航栏
        title_text.setText("中国");
        back_button.setVisibility(View.GONE);
        //从数据库查找
        provinces = DataSupport.findAll(Province.class);
        if (provinces.size() > 0){//从数据库查询到了数据
            //清空适配器数据集合
            dataList.clear();
            //将查询到的省份名称添加到适配器数据集合
            for (Province province : provinces){
                dataList.add(province.getProvinceName());
            }
            //刷新列表
            adapter.notifyDataSetChanged();
            list_view.setSelection(0);
            //更改当前选中级别
            currentLevel = LEVEL_PROVINCE;
        }else{//数据库没有相关数据则从网络请求
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,LEVEL_PROVINCE);
        }

    }
    /**
     * 查询城市
     */
    private void queryCity(){
        //设置导航栏
        title_text.setText(selectProvince.getProvinceName());
        back_button.setVisibility(View.VISIBLE);
        //从数据库查找
        cities = DataSupport.where("provinceid=?", String.valueOf(selectProvince.getId())).find(City.class);
        if (cities.size() > 0){//从数据库查询到了数据
            //清空适配器数据集合
            dataList.clear();
            //将查询到的城市名称添加到适配器数据集合
            for (City city : cities){
                dataList.add(city.getCityName());
            }
            //刷新列表
            adapter.notifyDataSetChanged();
            list_view.setSelection(0);
            //更改当前选中级别
            currentLevel = LEVEL_CITY;
        }else{//数据库没有相关数据则从网络请求
            String address = "http://guolin.tech/api/china/" + selectProvince.getProvinceCode();
            queryFromServer(address,LEVEL_CITY);
        }

    }
    /**
     * 查询县
     */
    private void queryCounty(){
        //设置导航栏
        title_text.setText(selectCity.getCityName());
        back_button.setVisibility(View.VISIBLE);
        //从数据库查找
        counties = DataSupport.where("cityid=?", String.valueOf(selectCity.getId())).find(County.class);
        if (counties.size() > 0){//从数据库查询到了数据
            //清空适配器数据集合
            dataList.clear();
            //将查询到的县名称添加到适配器数据集合
            for (County county : counties){
                dataList.add(county.getCountyName());
            }
            //刷新列表
            adapter.notifyDataSetChanged();
            list_view.setSelection(0);
            //更改当前选中级别
            currentLevel = LEVEL_COUNTY;
        }else{//数据库没有相关数据则从网络请求
            String address = "http://guolin.tech/api/china/" + selectProvince.getProvinceCode() + "/" + selectCity.getCityCode();
            queryFromServer(address,LEVEL_COUNTY);
        }
    }

    /**
     * 从服务器查询地区信息
     * @param address
     * @param type 请求类型
     */
    private void queryFromServer(String address, final int type) {
        //主线程显示进度
        showProgressDialog();
        //子线程发送请求
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean result = false;//记录存储数据是否成功
                String responseText = response.body().string();//请求的json数据
                //调用函数存储数据到数据库
                if (type == LEVEL_PROVINCE){
                    result = Utility.handleProvinceResponse(responseText);
                }else if (type == LEVEL_CITY){
                    result = Utility.handleCityResponse(selectProvince.getId(),responseText);
                }else if (type == LEVEL_COUNTY){
                    result = Utility.handleCountyResponse(selectCity.getId(),responseText);
                }
                //成功请求，再判断是否存储数据成功,并切换到主线程更新UI
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //关闭进程对话框
                            closeProgressDialog();
                            //显示列表
                            if (type == LEVEL_PROVINCE){
                                queryProvince();
                            }else if (type == LEVEL_CITY){
                                queryCity();
                            }else if (type == LEVEL_COUNTY){
                                queryCounty();
                            }
                        }
                    });
                }
            }
        });

    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
}
