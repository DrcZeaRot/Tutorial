## 静态广播

使用流程：
* 继承BroadcastReceiver类，并重写onReceive方法
* 在清单文件中注册对应receiver
* 某些系统级广播在高版本API中无法通过静态广播接收


##### 自定义广播接收者：

    SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @SuppressLint("ObsoleteSdkInt")
    class DownloadUtil {
        private final static Map<String, String> ACTION_MAP = new HashMap<>();

        static {
            ACTION_MAP.put(Intent.ACTION_BOOT_COMPLETED, "开机完成");
            ACTION_MAP.put(Intent.ACTION_USER_PRESENT, "屏幕解锁完成");
            ACTION_MAP.put(Intent.ACTION_SCREEN_ON, "屏幕点亮");
            ACTION_MAP.put(Intent.ACTION_SCREEN_OFF, "屏幕关闭");
            ACTION_MAP.put(Intent.ACTION_PACKAGE_ADDED, "安装App");
            ACTION_MAP.put(Intent.ACTION_PACKAGE_REMOVED, "卸载App");
            ACTION_MAP.put(Intent.ACTION_LOCALE_CHANGED, "位置发生变化");
            ACTION_MAP.put(Intent.ACTION_POWER_CONNECTED, "接通充电器");
            ACTION_MAP.put(Intent.ACTION_POWER_DISCONNECTED, "断开充电器");
            ACTION_MAP.put(Intent.ACTION_BATTERY_CHANGED, "电量发生变化");
            ACTION_MAP.put(Intent.ACTION_TIME_CHANGED, "时间发生变化");
            ACTION_MAP.put(Intent.ACTION_TIME_TICK, "计时器变化");
            ACTION_MAP.put(Intent.ACTION_CAMERA_BUTTON, "按下照相时的拍照按键");
            ACTION_MAP.put(Intent.ACTION_TIMEZONE_CHANGED, "时区发生变化");
            ACTION_MAP.put(Intent.ACTION_CLOSE_SYSTEM_DIALOGS, "屏幕超时，进行锁屏时");
            ACTION_MAP.put(Intent.ACTION_CONFIGURATION_CHANGED, "设备当前设置被改变时");
            ACTION_MAP.put(Intent.ACTION_HEADSET_PLUG, "插上耳机");
            ACTION_MAP.put("com.xcstasy.r.custom_receiver", "自定义Action");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                ACTION_MAP.put(Intent.ACTION_WALLPAPER_CHANGED, "墙壁纸发生改变");
            }

        }

        static String getActionCnName(String action) {
            LogTrack.w("action = " + action);
            return ACTION_MAP.get(action);
        }
    }
    ```

    ● 广播接收者

    ```
    public class DownloadAssistantBroadcastReceiver extends BroadcastReceiver {

        @SuppressLint("UnsafeProtectedBroadcastReceiver")
        @Override
        public void onReceive(Context context, Intent intent) {
            LogTrack.i("onReceive  " + DownloadUtil.getActionCnName(intent.getAction()));
        }


        @Override
        public IBinder peekService(Context myContext, Intent service) {
            LogTrack.i("peekService");
            return super.peekService(myContext, service);
        }
    }

注册清单文件：

    <receiver
        android:name=".back.DownloadAssistantBroadcastReceiver"
        android:enabled="true"
        android:exported="true"
        android:permission="android.permission.RECEIVE_BOOT_COMPLETED">
        <intent-filter>
            <category android:name="android.intent.category.LAUNCHER"/>
            <!--手机开机 广播-->
            <action android:name="android.intent.action.BOOT_COMPLETED"/>
            <!-- 手机屏幕解锁 广播 -->
            <action android:name="android.intent.action.USER_PRESENT"/>
            <!--手机屏幕 关闭  广播-->
            <action android:name="android.intent.action.SCREEN_OFF"/>
            <!--手机屏幕 点亮  广播-->
            <action android:name="android.intent.action.SCREEN_ON"/>
            <!--安装 app 广播-->
            <action android:name="android.intent.action.PACKAGE_ADDED"/>
            <!--卸载 app 广播-->
            <action android:name="android.intent.action.PACKAGE_REMOVED"/>
            <!--位置变化 广播-->
            <action android:name="android.intent.action.LOCALE_CHANGED"/>
            <!--接通充电器 广播-->
            <action android:name="android.intent.action.ACTION_POWER_CONNECTED"/>
            <!--断开充电器 广播-->
            <action android:name="android.intent.action.ACTION_POWER_DISCONNECTED"/>
            <action android:name="android.intent.action.TIME_SET"/>
            <!--计时器变化-->
            <action android:name="android.intent.action.TIME_TICK"/>
            <!--时区发生变化-->
            <action android:name="android.intent.action.TIMEZONE_CHANGED"/>
            <!--电量发生变化-->
            <action android:name="android.intent.action.BATTERY_CHANGED"/>
            <!--按下照相时的拍照按键-->
            <action android:name="android.intent.action.CAMERA_BUTTON"/>
            <!--屏幕超时，进行锁屏时-->
            <action android:name="android.intent.action.CLOSE_SYSTEM_DIALOGS"/>
            <!--设备当前设置被改变时-->
            <action android:name="android.intent.action.CONFIGURATION_CHANGED"/>
            <!--插上耳机-->
            <action android:name="android.intent.action.HEADSET_PLUG"/>
            <!--墙壁纸发生改变-->
            <action android:name="android.intent.action.WALLPAPER_CHANGED"/>


            <!--自定义Action-->
            <action android:name="com.xcstasy.r.custom_receiver"/>



        </intent-filter>
    </receiver>



##### 有序广播/拦截广播

通过priority控制广播的优先级

    <receiver
            android:name=".PriorityBroadcastReceiverLow"
            android:enabled="true"
            android:exported="true"
            android:permission="android.permission.RECEIVE_BOOT_COMPLETED">
    <intent-filter android:priority="1000">
            <action android:name="com.xcstasy.r.priority_receiver" />
    </intent-filter>
    </receiver>
    <receiver
            android:name=".PriorityBroadcastReceiverMid"
            android:enabled="true"
            android:exported="true"
            android:permission="android.permission.RECEIVE_BOOT_COMPLETED">
    <intent-filter android:priority="2000">
            <action android:name="com.xcstasy.r.priority_receiver" />
    </intent-filter>
    </receiver>
    <receiver
            android:name=".PriorityBroadcastReceiverHigh"
            android:enabled="true"
            android:exported="true"
            android:permission="android.permission.RECEIVE_BOOT_COMPLETED">
    <intent-filter android:priority="3000">
            <action android:name="com.xcstasy.r.priority_receiver" />
    </intent-filter>
    </receiver>


发送、拦截有序广播

    sendOrderBroadcast(intent,null)//发送有序广播

    high: fun onReceive(context:Context,intent:Intent){
            val code = 100
            val data = "high"
            val extras = Bundle()
            setResult(code,data,extras)
        }
    mid: fun onReceive(context:Context,intent:Intent){
            val code = resultCode / 2
            val data = "$resultData , mid"
            val extras = Bundle()
            Log.i("TAG","$resultCode, $resultData")
            abortBroadcast() //拦截有序广播
            setResult(code,data,extras)
        }
    low: fun onReceive(context:Context,intent:Intent){
            Log.i("TAG","$resultCode, $resultData")
        }

    sendOrderBroadcast(intent,null,
                        PriorityBroadcastReceiverLow(),//设置终结广播
                        Handler(),0,null,null)
    终结广播一定会被通知