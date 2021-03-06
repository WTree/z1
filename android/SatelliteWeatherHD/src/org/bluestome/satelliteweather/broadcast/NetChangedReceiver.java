
package org.bluestome.satelliteweather.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.bluestome.android.utils.HttpClientUtils;

import org.bluestome.satelliteweather.MainApp;
import org.bluestome.satelliteweather.services.UpdateService;

/**
 * @ClassName: NetChangedReceiver
 * @Description: 连网状态改变
 * @author Michael.Pan
 * @date 2012-2-22 下午02:41:21
 */
public class NetChangedReceiver extends BroadcastReceiver {
    private static final String TAG = NetChangedReceiver.class.getSimpleName();
    public static final String ACTION_NET = "android.net.conn.CONNECTIVITY_CHANGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_NET)) {
            ConnectivityManager mgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            // 获取多网络对象
            NetworkInfo[] networks = mgr.getAllNetworkInfo();
            // 判断获取的网络数量并且判断是否有活动网络
            if (null != networks && networks.length > 0
                    && null != mgr.getActiveNetworkInfo()) {
                MainApp.i().getExecutorService().execute(new Runnable() {
                    @Override
                    public void run() {
                        // 执行一次Http的外部测试
                        if (HttpClientUtils
                                .validationURL("http://www.163.com")) {
                            Log.d(TAG, "\tzhang 网络事件广播  测试外网连接正常");
                            // 测试成功表明与外网连接正常
                            MainApp.i().setConnected(true);
                            // 发送广播
                            Intent i = new Intent();
                            i.setAction(UpdateService.NET_NOTIFY);
                            MainApp.i().sendBroadcast(i);
                        } else {
                            Log.d(TAG, "\tzhang 网络事件广播  测试外网连接失败");
                            MainApp.i().setConnected(false);
                        }
                    }
                });
            } else {
                Log.d(TAG, "\tzhang 网络事件广播  当前没有可用网络");
                MainApp.i().setConnected(false);
            }

        }
    }
}
