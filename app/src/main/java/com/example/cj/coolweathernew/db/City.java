package com.example.cj.coolweathernew.db;

import org.litepal.crud.DataSupport;

/**
 * Created by cj on 2017/3/4.
 */

public class City extends DataSupport{
    private int  id;
    private int provinceId;
    private String cityName;
    private int cityCode;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
