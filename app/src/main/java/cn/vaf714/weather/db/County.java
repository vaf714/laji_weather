package cn.vaf714.weather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Passerby_B on 2017/3/4.
 */

public class County  extends DataSupport {
    private int id;
    private String countyName;
    private String weatherId;
    private int cityId;

    @Override
    public String toString() {
        return "County{" +
                "id=" + id +
                ", countyName='" + countyName + '\'' +
                ", weatherId='" + weatherId + '\'' +
                ", cityId=" + cityId +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int countyId) {
        this.cityId = countyId;
    }
}
