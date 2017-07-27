package yang.fan.coolweather.util;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import yang.fan.coolweather.db.City;
import yang.fan.coolweather.db.County;
import yang.fan.coolweather.db.Province;


/**
 * Created by Administrator on 2017/7/27.
 */

public class JSONUtil {

    /**
     * 解析从服务器返回的省级数据, 并存入数据库
     * @param response [{"id":1,"name":"北京"},...]
     * @return 是否成功
     */
    public static boolean handleProvincesResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray provinces = new JSONArray(response);
                for (int i = 0; i < provinces.length(); i++) {
                    JSONObject provinceObj = provinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObj.getString("name"));
                    province.setProvinceCode(provinceObj.getInt("id"));
                    province.save(); // 将数据保存到数据库中
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析从服务器返回的市级数据, 并存入数据库
     * @param response [{"id":113,"name":"南京"},...]
     * @param provinceId
     * @return
     */
    public static boolean handleCitiesResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray cities = new JSONArray(response);
                for (int i = 0; i < cities.length(); i++) {
                    JSONObject cityObj = cities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObj.getString("name"));
                    city.setCityCode(cityObj.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save(); // 将数据保存到数据库中
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析从服务器返回的县级数据, 并存入数据库.
     * @param response [{"id":921,"name":"南京","weather_id":"CN101190101"},...]
     * @param cityId
     * @return
     */
    public static boolean handleCountiesResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray counties = new JSONArray(response);
                for (int i = 0; i < counties.length(); i++) {
                    JSONObject countyObj = counties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObj.getString("name"));
                    county.setWeatherId(countyObj.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save(); // 将数据保存到数据库中
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
