package yang.fan.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import yang.fan.coolweather.gson.Forecast;
import yang.fan.coolweather.gson.Weather;
import yang.fan.coolweather.serevice.AutoUpdateService;
import yang.fan.coolweather.util.HttpUtil;
import yang.fan.coolweather.util.JSONUtil;

public class WeatherActivity extends AppCompatActivity {

    @BindView(R.id.iv_weather_bg)
    ImageView mIvWeatherBg;
    @BindView(R.id.srl_weather_refresh)
    public SwipeRefreshLayout mSrlWeatherRefresh;
    @BindView(R.id.tv_title_city)
    TextView mTvTitleCity;
    @BindView(R.id.tv_title_update)
    TextView mTvTitleUpdate;
    @BindView(R.id.tv_now_degree)
    TextView mTvNowDegree;
    @BindView(R.id.tv_now_weather_info)
    TextView mTvNowWeatherInfo;
    @BindView(R.id.ll_forecast_layout)
    LinearLayout mLlForecastLayout;
    @BindView(R.id.tv_aqi_text)
    TextView mTvAqiText;
    @BindView(R.id.tv_aqi_pm25)
    TextView mTvAqiPm25;
    @BindView(R.id.tv_suggestion_comfort)
    TextView mTvSuggestionComfort;
    @BindView(R.id.tv_suggestion_car_wash)
    TextView mTvSuggestionCarWash;
    @BindView(R.id.tv_suggestion_sport)
    TextView mTvSuggestionSport;
    @BindView(R.id.sv_weather_layout)
    ScrollView mSvWeatherLayout;
    @BindView(R.id.btn_title_nav)
    Button mBtnTitleNav;
    @BindView(R.id.dl_weather_layout)
    public DrawerLayout mDlWeatherLayout;
    private String mWeatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏状态栏
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);

        mSrlWeatherRefresh.setColorSchemeResources(R.color.colorPrimary);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bgPicUrl = prefs.getString("bg_pic_url", null);
        if (bgPicUrl != null) {
            Glide.with(this).load(bgPicUrl).into(mIvWeatherBg);
        } else {
            loadBackgroundPic();
        }
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            // 有缓存时直接解析天气数据
            Weather weather = JSONUtil.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            // 无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            mSvWeatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        mSrlWeatherRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        mBtnTitleNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDlWeatherLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * 从服务器加载背景图片
     */
    private void loadBackgroundPic() {
        String requestUrl = getResources().getString(R.string.BASE_URL) + "bing_pic";
        HttpUtil.sendOkHttpRequest(requestUrl, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bgPicUrl = response.body().string();
                SharedPreferences.Editor edit = PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this).edit();
                edit.putString("bg_pic_url", bgPicUrl);
                edit.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bgPicUrl).into(mIvWeatherBg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 根据天气 id 请求城市天气信息
     *
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = getResources().getString(R.string.BASE_URL) + "weather?cityid="
                + weatherId + "&key=" + getResources().getString(R.string.KEY);
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = JSONUtil.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor edit = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            edit.putString("weather", responseText);
                            mWeatherId = weatherId;
                            edit.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败"
                                    , Toast.LENGTH_SHORT).show();
                        }
                        mSrlWeatherRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败"
                                , Toast.LENGTH_SHORT).show();
                        mSrlWeatherRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBackgroundPic();
    }

    /**
     * 处理并展示 weather 实体类中的数据
     *
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        mTvTitleCity.setText(cityName);
        mTvTitleUpdate.setText(updateTime);
        mTvNowDegree.setText(degree);
        mTvNowWeatherInfo.setText(weatherInfo);
        mLlForecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_forecast
                    , mLlForecastLayout, false);
            TextView tvDate = view.findViewById(R.id.tv_item_forecast_date);
            TextView tvInfo = view.findViewById(R.id.tv_item_forecast_info);
            TextView tvMax = view.findViewById(R.id.tv_item_forecast_max);
            TextView tvMin = view.findViewById(R.id.tv_item_forecast_min);
            tvDate.setText(forecast.date);
            tvInfo.setText(forecast.more.info);
            tvMax.setText(forecast.temperature.max);
            tvMin.setText(forecast.temperature.min);
            mLlForecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            mTvAqiText.setText(weather.aqi.city.aqi);
            mTvAqiPm25.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度: " + weather.suggestion.comfort.info;
        String carWash = "洗车指数: " + weather.suggestion.carWash.info;
        String sport = "运动建议: " + weather.suggestion.sport.info;
        mTvSuggestionComfort.setText(comfort);
        mTvSuggestionCarWash.setText(carWash);
        mTvSuggestionSport.setText(sport);
        mSvWeatherLayout.setVisibility(View.VISIBLE);

        // 开启定时服务, 每8小时更新一次天气
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
}
