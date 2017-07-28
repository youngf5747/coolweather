package yang.fan.coolweather.serevice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import yang.fan.coolweather.R;
import yang.fan.coolweather.gson.Weather;
import yang.fan.coolweather.util.HttpUtil;
import yang.fan.coolweather.util.JSONUtil;

public class AutoUpdateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBgPic();
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int eightHour = 8 * 3600 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + eightHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气信息
     */
    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            // 有缓冲时直接解析天气
            Weather weather = JSONUtil.handleWeatherResponse(weatherString);
            final String weatherId = weather.basic.weatherId;
            String weatherUrl = getResources().getString(R.string.BASE_URL) + "weather?cityid=?"
                    + weatherId + "&key=" + getResources().getString(R.string.KEY);
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = JSONUtil.handleWeatherResponse(responseText);
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences.Editor edit = PreferenceManager
                                .getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        edit.putString("weather", responseText);
                        edit.apply();
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * 更新背景图片
     */
    private void updateBgPic() {
        String requestUrl = getResources().getString(R.string.BASE_URL) + "bing_pic";
        HttpUtil.sendOkHttpRequest(requestUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bgPicUrl = response.body().string();
                SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(
                        AutoUpdateService.this).edit();
                edit.putString("bg_pic_url", bgPicUrl);
                edit.apply();
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }
}
