package com.sprocomm.gprstest.application;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.text.TextUtils;

import com.fitsleep.sunshinelibrary.utils.ToastUtils;
import com.sprocomm.gprstest.config.Configure;
import com.sprocomm.gprstest.utils.LogUtils;
import com.sunshine.blelibrary.inter.IBLE;
import com.sunshine.blelibrary.service.BLEService;

/**
 * Created by yuanbin.ning on 2017/6/5.
 */

public class App extends Application {

    private static App sApp;
    private BLEService mBleService;

    private static final String SHARED_VALUES = "diagnostic_settings_value";
    public static final String SHARED_KEY_SETTINGS_VALUE = "case_values";
    public static final String SHARED_KEY_CONFIG_IP = "server_ip";
    public static final String SHARED_KEY_CONFIG_METHOD = "upload_method";

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        ToastUtils.init(this);
        CrashHandler.getInstance().init(this);
        initBle();
        updateConfigs();
    }

    private void initBle() {
        Intent intent = new Intent(this, BLEService.class);
        boolean bindService = bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        if (bindService){
            LogUtils.e(App.class.getSimpleName(),"蓝牙服务启动成功");
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            ToastUtils.showMessage("不支持BLE");
            return;
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter ==null){
            ToastUtils.showMessage("不支持BLE");
            return;
        }
        if (!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBleService = ((BLEService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBleService = null;
        }
    };

    public IBLE getIBLE(){
        return mBleService.getIBLE();
    }

    public static App getInstance(){
        return sApp;
    }

    private void updateConfigs() {
        String savedIp = sharedGet(App.SHARED_KEY_CONFIG_IP);
        String savedMethod = sharedGet(App.SHARED_KEY_CONFIG_METHOD);
        if (!TextUtils.isEmpty(savedIp)) {
            Configure.SERVER_IP = savedIp;
        }
        if (!TextUtils.isEmpty(savedMethod)) {
            Configure.METHOD_NAME = savedMethod;
        }
    }

    public void sharedSave(String key, String value) {
        SharedPreferences shared = getSharedPreferences(SHARED_VALUES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String sharedGet(String key) {
        SharedPreferences shared = getSharedPreferences(SHARED_VALUES, Context.MODE_PRIVATE);
        String value = shared.getString(key, "");
        return value;
    }
}