package yang.fan.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Administrator on 2017/7/27.
 */

public class HttpUtil {

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        // enqueue()方法的内部会开启子线程, 故此 callback回调方法也会运行在子线程中
        client.newCall(request).enqueue(callback);
    }
}
