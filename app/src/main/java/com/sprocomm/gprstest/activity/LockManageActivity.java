package com.sprocomm.gprstest.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.fitsleep.sunshinelibrary.utils.ActManager;
import com.fitsleep.sunshinelibrary.utils.DialogUtils;
import com.fitsleep.sunshinelibrary.utils.MPermissionsActivity;
import com.fitsleep.sunshinelibrary.utils.ToastUtils;
import com.sprocomm.gprstest.GpsMainActivity;
import com.sprocomm.gprstest.LockActivity;
import com.sprocomm.gprstest.R;
import com.sprocomm.gprstest.adapter.TestCaseAdapter;
import com.sprocomm.gprstest.application.App;
import com.sprocomm.gprstest.bean.OneCase;
import com.sprocomm.gprstest.bean.SettingsCase;
import com.sprocomm.gprstest.bean.TestCase;
import com.sprocomm.gprstest.bean.TwoCase;
import com.sprocomm.gprstest.config.Configure;
import com.sprocomm.gprstest.utils.LogUtils;
import com.sprocomm.gprstest.utils.Utils;
import com.sunshine.blelibrary.config.Config;
import com.sunshine.blelibrary.config.LockType;
import com.sunshine.blelibrary.inter.OnConnectionListener;
import com.sunshine.blelibrary.utils.GlobalParameterUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;


/**
 * Created by yuanbin.ning on 2017/6/6.
 */

public class LockManageActivity extends MPermissionsActivity implements OnConnectionListener {

    private String mName;
    private String mAddress;

    private Dialog loadingDialog;
    private List<SettingsCase> mSettingsValue = new ArrayList<>();
    private List<OneCase> mData = new ArrayList<>();

    private List<TestCase> testData = new ArrayList<>();

    private ListView mListView;
    private TestCaseAdapter adapter;
    private TextView mUploadResult;

    private int openTotalTimes = 0;
    private int openSuccessTimes = 0;

    private  static final int SEND_5 = 5;

    private int selectMode = -1;
    private SharedPreferences sp;
    SharedPreferences.Editor editor;

//    private boolean mConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Configure.METHOD_NAME.equals(Configure.METHOD_SUB_SEMI_BT_TEST)) {
            setTitle(R.string.test_semi_bt);
        } else if (Configure.METHOD_NAME.equals(Configure.METHOD_SUB_BT_TEST)) {
            setTitle(R.string.test_bt);
        }
        setContentView(R.layout.activity_lock_manage);
        sp = getSharedPreferences("save_imei",Context.MODE_PRIVATE);
        editor = sp.edit();
        initViews();
        initData();
        ActManager.getAppManager().addActivity(this);
        registerReceiver(broadcastReceiver, Config.initFilter());

        GlobalParameterUtils.getInstance().setLockType(LockType.MTS);
        if (!TextUtils.isEmpty(mAddress)) {
            App.getInstance().getIBLE().connect(mAddress, this);
            loadingDialog = DialogUtils.getLoadingDialog(this, "");
            loadingDialog.show();
        }
    }

    private void initViews() {
        mListView = (ListView) findViewById(R.id.testcase_listview);
        mUploadResult = (TextView) findViewById(R.id.upload_result);
    }

    private void initData() {
        webServiceUrl = String.format(getString(R.string.upload_service_url), Configure.SERVER_IP);
        mName = getIntent().getStringExtra("name");
        mAddress = getIntent().getStringExtra("address");
        String savedSettings = App.getInstance().sharedGet(App.SHARED_KEY_SETTINGS_VALUE);
        if (!TextUtils.isEmpty(savedSettings)) {
            try {
                mSettingsValue.clear();
                JSONArray jsonArray = new JSONArray(savedSettings);
                for (int i = 0; i < jsonArray.length(); ++i) {
                    SettingsCase sc = new SettingsCase(jsonArray.getJSONObject(i));
                    mSettingsValue.add(sc);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            mSettingsValue = Utils.getDefaultValues(this);
        }
        for (int i = 0; i < mSettingsValue.size(); ++i) {
            OneCase oc = new OneCase();
            SettingsCase sc = mSettingsValue.get(i);
            oc.setName(sc.getName());
            oc.setState(sc.isEnable() ? 1 : 0);
            mData.add(oc);
        }
        mData.get(0).setValue(mName);
        mData.get(1).setValue(mAddress);
        mData.get(17).setValue(String.format(getString(R.string.open_value),
                openTotalTimes, openSuccessTimes));
        updateData();
        adapter = new TestCaseAdapter(this, testData);
        mListView.setAdapter(adapter);
    }

    private void update() {
        updateData();
        adapter.notifyDataSetChanged();
    }

    private void updateData() {
        testData.clear();
        for (int i = 0; i < mSettingsValue.size(); ++i) {
            if (i > 1 && i < 8) {
                OneCase oc2 = mData.get(i);
                boolean oc2test = mSettingsValue.get(i).isTest();
                i += 1;
                OneCase oc3 = mData.get(i);
                boolean oc3test = mSettingsValue.get(i).isTest();
                TwoCase tc = new TwoCase();
                tc.setCase(oc2, oc3);
                if (oc2test || oc3test) {
                    testData.add(tc);
                }
            } else if (mSettingsValue.get(i).isTest()) {
                testData.add(mData.get(i));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        TestCase imei = mData.get(19);
        if(imei.getValue() == null) {
            editor.putString("imei_toString", null);
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String data = intent.getStringExtra("data");
            String imeiString = null;
            int workMode = -1;
            switch (action) {
                case Config.TOKEN_ACTION:
                    m_myHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            App.getInstance().getIBLE().getGSMId();
                        }
                    }, 300);
                    TestCase versionTc = mData.get(5);
                    String version = GlobalParameterUtils.getInstance().getVersion();
                    LogUtils.d("TAG", version);
                    versionTc.setValue(version);
                    if (version.equals(mSettingsValue.get(5).getValue())) {
                        versionTc.setPass(true);
                    } else {
                        versionTc.setPass(false);
                    }
                    break;
                case Config.GSM_ID_ACTION:
                    updateButtonView(R.id.btn_gsm_id);
                    m_myHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            App.getInstance().getIBLE().getMode();
                        }
                    }, 300);
                    TestCase gsmIdTc = mData.get(3);
                    if (TextUtils.isEmpty(data)) {
                        gsmIdTc.setValue("空");
                        gsmIdTc.setPass(false);
                    } else {
                        gsmIdTc.setValue(data);
                        gsmIdTc.setPass(true);
                    }
                    break;
                case Config.GET_MODE:
                    m_myHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            App.getInstance().getIBLE().getWorkStatus();
                        }
                    }, 300);
                    TestCase modeTc = mData.get(7);
                    if (TextUtils.isEmpty(data)) {
                        modeTc.setValue("运输");
                        workMode = 0;
                    } else {
                        modeTc.setValue("正常");
                        workMode = 1;
                    }
                    editor.putInt("work_mode",workMode);
                    break;
                case Config.GET_WORK_STATUS:
                    updateButtonView(R.id.btn_work_state);
                    if (!isTestCaseFinished) {
                        m_myHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                App.getInstance().getIBLE().getGSMVersion();
                            }
                        }, 300);
                    }
                    // 查询锁的工作状态
                    TestCase lockState = mData.get(4);
                    int r0 = Integer.parseInt(data.substring(6, 8), 16);
                    if ((r0 & 1) == 0) {
                        lockState.setValue("开");
                    } else {
                        lockState.setValue("关");
                    }
                    TestCase vibration = mData.get(6);
                    if (((r0 >> 1) & 1) == 0) {
                        vibration.setValue("正常");
                    } else {
                        vibration.setValue("故障");
                    }
                    TestCase vibrateState = mData.get(8);
                    if (((r0 >> 2) & 1) == 0) {
                        vibrateState.setValue("静止");
                    } else {
                        vibrateState.setValue("振动");
                    }
                    TestCase battery = mData.get(9);
                    int r1 = Integer.parseInt(data.substring(8, 10), 16);
                    battery.setValue(String.valueOf(r1));
                    battery.setPass(Utils.compare(r1, mSettingsValue.get(9).getValue()));
                    TestCase gsmTc = mData.get(10);
                    int r2 = Integer.parseInt(data.substring(10, 12), 16);
                    int gsmState;
                    switch (r2) {
                        case 0:
                            gsmState = 0;
                            break;
                        case 1:
                            gsmState = 1;
                            break;
                        case 2:
                            gsmState = 2;
                            break;
                        case 3:
                            gsmState = 3;
                            break;
                        case 4:
                            gsmState = 4;
                            break;
                        case 5:
                            gsmState = 5;
                            break;
                        case 6:
                            gsmState = 6;
                            break;
                        case 7:
                            gsmState = 7;
                            break;
                        case 0xFE:
                            gsmState = 8;
                            break;
                        case 0xFF:
                            gsmState = 9;
                            break;
                        default:
                            gsmState = 0;
                            break;
                    }
                    gsmTc.setValue(getResources().getStringArray(R.array.gsm_state)[gsmState]);
                    gsmTc.setState(1);
                    if (r2 == 7) {
                        gsmTc.setPass(true);
                        isGsmStatePassed = true;
                        // TODO upload
                        upLoadTestData();
                    } else {
                        gsmTc.setPass(false);
                        isGsmStatePassed = false;
                        upLoadTestData();
                    }
                    TestCase gprsTime = mData.get(11);
                    int r3 = Integer.parseInt(data.substring(12, 14), 16);
                    gprsTime.setValue(String.valueOf(r3));
                    gprsTime.setPass(Utils.compare(r3, mSettingsValue.get(11).getValue()));
                    TestCase gsmLv = mData.get(12);
                    int r4 = Integer.parseInt(data.substring(14, 16), 16);
                    gsmLv.setValue(String.valueOf(r4));
                    gsmLv.setPass(Utils.compare(r4, mSettingsValue.get(12).getValue()));
                    TestCase gpsTc = mData.get(13);
                    int r5 = Integer.parseInt(data.substring(16, 18), 16);
                    int gpsState;
                    switch (r5) {
                        case 0:
                            gpsState = 0;
                            break;
                        case 1:
                            gpsState = 1;
                            break;
                        case 2:
                            gpsState = 2;
                            break;
                        case 0xFF:
                            gpsState = 3;
                            break;
                        default:
                            gpsState = 0;
                            break;
                    }
                    gpsTc.setValue(getResources().getStringArray(R.array.gps_state)[gpsState]);
                    gpsTc.setState(1);
                    if (r5 == 2) {
                        gpsTc.setPass(true);
                    } else {
                        gpsTc.setPass(false);
                    }
                    TestCase gpsTime = mData.get(14);
                    int r6 = Integer.parseInt(data.substring(18, 20), 16);
                    gpsTime.setValue(String.valueOf(r6));
                    gpsTime.setPass(Utils.compare(r6, mSettingsValue.get(14).getValue()));
                    TestCase gpsStar = mData.get(15);
                    int r7 = Integer.parseInt(data.substring(20, 22), 16);
                    gpsStar.setValue(String.valueOf(r7));
                    gpsStar.setPass(Utils.compare(r7, mSettingsValue.get(15).getValue()));
                    break;
                case Config.GSM_VERSION_ACTION:
                    updateButtonView(R.id.btn_gsm_version);
                    m_myHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            App.getInstance().getIBLE().getICCID();
                        }
                    }, 300);
                    // 查询锁的GSM 版本号
                    TestCase gsmVersion = mData.get(16);
                    if (TextUtils.isEmpty(data)) {
                        gsmVersion.setValue("空");
                        gsmVersion.setPass(false);
                    } else {
                        byte[] ascii = Utils.getHexBytes(data);
                        String gsmVerStr = Utils.asciiToStr(ascii);
                        gsmVersion.setValue(gsmVerStr);
                        gsmVersion.setPass(gsmVerStr.equals(mSettingsValue.get(16).getValue()));
                    }
                    break;
                case Config.GET_ICCID_ACTION:
                    updateButtonView(R.id.btn_iccid);
                    m_myHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            App.getInstance().getIBLE().getIMEI();
                        }
                    }, 300);
                    TestCase iccid = mData.get(18);
                    if (TextUtils.isEmpty(data)) {
                        iccid.setValue("空");
                        iccid.setPass(false);
                    } else {
                        iccid.setValue(data);
                        iccid.setPass(true);
                    }
                    break;
                case Config.GET_IMEI_ACTION:
                    updateButtonView(R.id.btn_imei);
                    m_myHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            App.getInstance().getIBLE().getDomain();
                        }
                    }, 300);
                    TestCase imei = mData.get(19);
                    if (TextUtils.isEmpty(data)) {
                        imei.setValue("空");
                        imei.setPass(false);
                        imeiString = null;
                    } else {
                        byte[] bcdImei = Utils.getHexBytes(data);
                        imeiString = Utils.bcd2Str(bcdImei).substring(0, 15);
                        imei.setValue(imeiString);
                        imei.setPass(true);
                    }
                    editor.putString("imei_toString",imeiString);
                    break;
                case Config.GET_DOMAIN_ACTION:
                    if (!TextUtils.isEmpty(data)) {
                        domainStr = new StringBuilder();
                        byte[] ascii1 = Utils.getHexBytes(data);
                        domainStr.append(Utils.asciiToStr(ascii1));
                    }
                    break;
                case Config.GET_DOMAIN2_ACTION:
                    updateButtonView(R.id.btn_domain);
                    m_myHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            App.getInstance().getIBLE().getIP();
                        }
                    }, 300);
                    TestCase domain = mData.get(20);
                    if (domainStr != null) {
                        if (TextUtils.isEmpty(data)) {
                            domain.setValue(domainStr.toString());
                        } else {
                            byte[] ascii2 = Utils.getHexBytes(data);
                            domainStr.append(Utils.asciiToStr(ascii2));
                            domain.setValue(domainStr.toString());
                        }
                        domain.setPass(domainStr.toString().equals(mSettingsValue.get(20).getValue()));
                    } else {
                        domain.setValue("空");
                        domain.setPass(false);
                    }
                    break;
                case Config.GET_IP_ACTION:
                    updateButtonView(R.id.btn_ip);
                    TestCase ipTc = mData.get(21);
                    if (TextUtils.isEmpty(data)) {
                        ipTc.setValue("空");
                        ipTc.setPass(false);
                    } else {
                        byte[] r22 = Utils.getHexBytes(data);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < r22.length; ++i) {
                            int ip_addr = r22[i];
                            if (r22[i] < 0) {
                                ip_addr &= 0xff;
                            }
                            sb.append(ip_addr);
                            if (i < r22.length - 1) {
                                sb.append('.');
                            }
                        }
                        ipTc.setValue(sb.toString());
                        ipTc.setPass(sb.toString().equals(mSettingsValue.get(21).getValue()));
                    }
                    isTestCaseFinished = true;
                    // TODO upload
                    upLoadTestData();
                    break;
                case Config.BATTERY_ACTION:
                    if (TextUtils.isEmpty(data)) {
//                        tvCz.setText("当前操作：获取电量失败");
                    } else {
//                        tvCz.setText("当前操作：获取电量成功");
//                        tvBattery.setText("当前电量：" + Integer.parseInt(data, 16));
                        m_myHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                App.getInstance().getIBLE().getLockStatus();
                            }
                        }, 300);
                    }
                    break;
                case Config.OPEN_ACTION:
                    updateButtonView(R.id.btn_open);
                    TestCase open = mData.get(17);
                    openTotalTimes += 1;
                    if (TextUtils.isEmpty(data)) {
//                        tvCz.setText("当前操作：开锁失败");
                    } else {
                        mData.get(4).setValue("开");
                        openSuccessTimes += 1;
                        try {
                            if (openTotalTimes >= Integer.parseInt(mSettingsValue.get(17).getValue())) {
                                if (openSuccessTimes == openTotalTimes) {
                                    open.setPass(true);
                                } else {
                                    open.setPass(false);
                                }
                                // 开锁测试结束
                                isOpenLockFinished = true;
                                // TODO upload
                                upLoadTestData();
                            }
                        } catch (NumberFormatException e) {
                        }
//                        tvCz.setText("当前操作：开锁成功");
                    }
                    open.setValue(String.format(getString(R.string.open_value),
                            openTotalTimes, openSuccessTimes));
                    break;
                case Config.CLOSE_ACTION:
                    if (TextUtils.isEmpty(data)) {
//                        tvCz.setText("当前操作：复位失败");
                    } else {
//                        tvCz.setText("当前操作：复位成功");
                    }
                    break;
                case Config.LOCK_STATUS_ACTION:
                    if (TextUtils.isEmpty(data)) {
                        mData.get(4).setValue("关");
                        m_myHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                App.getInstance().getIBLE().openLock();
                            }
                        }, 300);
                    } else {
                        mData.get(4).setValue("开");
//                        tvCz.setText("当前操作：锁已开启");
//                        update();
                    }
                    break;
                case Config.LOCK_RESULT:
                    updateButtonView(R.id.btn_reset);
                    if (TextUtils.isEmpty(data)) {
//                        tvCz.setText("当前操作：上锁超时");
                    } else {
                        mData.get(4).setValue("关");
//                        tvCz.setText("当前操作：上锁成功");
                    }
                    break;
                case Config.SET_MODE:
                    if (TextUtils.isEmpty(data)) {
                        switch (selectMode) {
                            case 0:
                                updateButtonView(R.id.btn_normal);
//                                tvCz.setText("当前操作：设置正常模式失败");

                                break;
                            case 1:
                                updateButtonView(R.id.btn_transport);
//                                tvCz.setText("当前操作：设置运输模式失败");
                                break;
                            case 2:
                                updateButtonView(R.id.btn_restart);
                                break;
                        }
                        workMode = -1;
                    } else {
                        switch (selectMode) {
                            case 0:
                                updateButtonView(R.id.btn_normal);
//                                tvCz.setText("当前操作：设置正常模式成功");
                                mData.get(7).setValue("正常");
                                workMode = 1;
                                break;
                            case 1:
                                updateButtonView(R.id.btn_transport);
//                                tvCz.setText("当前操作：设置运输模式成功");
                                mData.get(7).setValue("运输");
                                workMode = 0;
                                break;
                            case 2:
                                updateButtonView(R.id.btn_restart);
                                mData.get(7).setValue("重启");
                                break;
                        }
                    }
                    editor.putInt("work_mode",workMode);
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:

                            break;
                        case BluetoothAdapter.STATE_ON:
                            m_myHandler.sendEmptyMessageDelayed(0, 3000);
                            break;
                    }
                    break;
            }
            editor.commit();
            update();
        }
    };

    private StringBuilder domainStr;

    @Override
    public void onConnect() {
//        mConnected = true;
        mData.get(2).setValue("已连接");
        update();
//        invalidateOptionsMenu();
        LogUtils.d(getClass().getSimpleName(), "连接成功");
    }

    @Override
    public void onDisconnect(int state) {
//        mConnected = false;
        mData.get(2).setValue("已断开");
        update();
//        invalidateOptionsMenu();
        LogUtils.d(getClass().getSimpleName(), "断开连接");
        App.getInstance().getIBLE().connect(mAddress, this); // 自动连接
        loadingDialog = DialogUtils.getLoadingDialog(this, "正在连接");
        loadingDialog.show();
        if (GlobalParameterUtils.getInstance().isUpdate()) {
            App.getInstance().getIBLE().resetBluetoothAdapter();
        } else {
        }
    }

    @Override
    public void onServicesDiscovered(String name, String address) {

        if (null != loadingDialog && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
        if (GlobalParameterUtils.getInstance().isUpdate()) {
            LogUtils.e(getClass().getSimpleName(), "服务");
//            tvCz.setText("当前操作：进入固件升级模式");
        } else {
            LogUtils.e(getClass().getSimpleName(), "getToken...");
            getToken();
        }
    }

    /**
     * 获取token
     */
    private void getToken() {
        m_myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                App.getInstance().getIBLE().getToken();
            }
        }, 500);
    }
        public static boolean isForeground(Context context, String className) {
            if (context == null || TextUtils.isEmpty(className))
                return false;
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
            if (list != null && list.size() > 0) {
                ComponentName cpn = list.get(0).topActivity;
                if (className.equals(cpn.getClassName()))
                    return true;
            }
            return false;
        }
    public static boolean isForeground(Activity activity) {
        return isForeground(activity, activity.getClass().getName());
    }

    Handler m_myHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message mes) {
            switch (mes.what) {
                case 0:
                    App.getInstance().getIBLE().connect(mAddress, LockManageActivity.this);
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 4:
                    break;
                case SEND_5:
                    if(isForeground(LockManageActivity.this)){
                        ToastUtils.showMessage("正在跳转至GPS测试...");
                        Intent intent = new Intent(LockManageActivity.this,GpsMainActivity.class);
                        startActivity(intent);
                    }
                    break;
            }
            return false;
        }
    });

    private Button btn01;
    private Button btn02;
    private Button btn03;
    private Button btn04;
    private Button btn05;
    private Button btn06;
    private Button btn07;
    private Button btn08;
    private Button btn09;
    private Button btn10;
    private Button btn11;
    private Button btn12;
    private void updateButtonView(int btnId) {
        if (clickedTimes < 1) {
            return;
        }
        clickedTimes -= 1;
        Button btn = null;
        switch (btnId) {
            case R.id.btn_open:
                if (btn01 == null) {
                    btn01 = (Button) findViewById(btnId);
                }
                btn = btn01;
                break;
            case R.id.btn_reset:
                if (btn02 == null) {
                    btn02 = (Button) findViewById(btnId);
                }
                btn = btn02;
                break;
            case R.id.btn_work_state:
                if (btn03 == null) {
                    btn03 = (Button) findViewById(btnId);
                }
                btn = btn03;
                break;
            case R.id.btn_normal:
                if (btn04 == null) {
                    btn04 = (Button) findViewById(btnId);
                }
                btn = btn04;
                break;
            case R.id.btn_transport:
                if (btn05 == null) {
                    btn05 = (Button) findViewById(btnId);
                }
                btn = btn05;
                break;
            case R.id.btn_restart:
                if (btn06 == null) {
                    btn06 = (Button) findViewById(btnId);
                }
                btn = btn06;
                break;
            case R.id.btn_iccid:
                if (btn07 == null) {
                    btn07 = (Button) findViewById(btnId);
                }
                btn = btn07;
                break;
            case R.id.btn_imei:
                if (btn08 == null) {
                    btn08 = (Button) findViewById(btnId);
                }
                btn = btn08;
                break;
            case R.id.btn_domain:
                if (btn09 == null) {
                    btn09 = (Button) findViewById(btnId);
                }
                btn = btn09;
                break;
            case R.id.btn_ip:
                if (btn10 == null) {
                    btn10 = (Button) findViewById(btnId);
                }
                btn = btn10;
                break;
            case R.id.btn_gsm_id:
                if (btn11 == null) {
                    btn11 = (Button) findViewById(btnId);
                }
                btn = btn11;
                break;
            case R.id.btn_gsm_version:
                if (btn12 == null) {
                    btn12 = (Button) findViewById(btnId);
                }
                btn = btn12;
                break;
        }
        if (btn != null) {
            btn.setTextColor(Color.GREEN);
        }
    }
    int clickedTimes = 0;
    public void onClick(View view) {
        if (view instanceof Button) {
            clickedTimes += 1;
            ((Button) view).setTextColor(Color.BLUE);
        }
        int id = view.getId();
        switch (id) {
            case R.id.btn_open:
                App.getInstance().getIBLE().openLock();
                break;
            case R.id.btn_reset:
                App.getInstance().getIBLE().resetLock();
                break;
            case R.id.btn_work_state:
                App.getInstance().getIBLE().getWorkStatus();
                break;
            case R.id.btn_normal:
                selectMode = 0;
                App.getInstance().getIBLE().setMode(0);
                break;
            case R.id.btn_transport:
                selectMode = 1;
                App.getInstance().getIBLE().setMode(1);
                break;
            case R.id.btn_restart:
                selectMode = 2;
                App.getInstance().getIBLE().setMode(2);
                break;
            case R.id.btn_iccid:
                App.getInstance().getIBLE().getICCID();
                break;
            case R.id.btn_imei:
                App.getInstance().getIBLE().getIMEI();
                break;
            case R.id.btn_domain:
                App.getInstance().getIBLE().getDomain();
                break;
            case R.id.btn_ip:
                App.getInstance().getIBLE().getIP();
                break;
            case R.id.btn_gsm_id:
                App.getInstance().getIBLE().getGSMId();
                break;
            case R.id.btn_gsm_version:
                App.getInstance().getIBLE().getGSMVersion();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
        m_myHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        App.getInstance().getIBLE().close();
    }

    /**
     * 上传必备条件
     * 1.开锁测试结束
     * 2.GSM 状态 pass
     * 3.测试完成
     */
    boolean isOpenLockFinished = false;
    boolean isGsmStatePassed = false;
    boolean isTestCaseFinished = false;
    private void upLoadTestData() {
        if (!isGsmStatePassed) {
            mUploadResult.setText(String.format(getString(R.string.upload_result), getString(R.string.upload_result_gsm)));
            return;
        }
        if (!isTestCaseFinished) {
            mUploadResult.setText(String.format(getString(R.string.upload_result), getString(R.string.upload_result_in_case)));
            return;
        }
        if (mSettingsValue.get(17).isTest() && !isOpenLockFinished) {
            mUploadResult.setText(String.format(getString(R.string.upload_result), getString(R.string.upload_result_open_lock)));
            return;
        }
        mUploadResult.setText(String.format(getString(R.string.upload_result), getString(R.string.upload_result_start)));
        if (Utils.isNetworkAvailable(this)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String sn = mData.get(19).getValue(); // IMEI
                    if (TextUtils.isEmpty(sn)) {
                        sn = mData.get(3).getValue(); // GSM ID
                    }
                    if (!TextUtils.isEmpty(sn)) {
                        getRemoteInfo(sn, "username", getAllTestResult());
                    }
                }
            }).start();
        } else {
            ToastUtils.showMessage(R.string.network_none);
        }
    }

    private boolean getAllTestResult() {
        for (int i = 0; i < testData.size(); ++i) {
            TestCase tc = testData.get(i);
            if (tc.getState() == 1) { // 显示pass测项
                if (!tc.isPass()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String webServiceUrl;
    private static final String SN = "sn";
    private static final String USERNAME = "testUser";
    private static final String FLAG = "flag";

    private void getRemoteInfo(String imei, String username, boolean isPass) {
        SoapObject rpc = new SoapObject(Configure.NAME_SPACE, Configure.METHOD_NAME);
        int length = imei.length();
        if (length < 12) {
            rpc.addProperty(SN, imei);
        } else {
            rpc.addProperty(SN, imei.substring(length - 12, imei.length()));
        }
        rpc.addProperty(USERNAME, username);
        rpc.addProperty(FLAG, isPass ? 1 : 0);
        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER12);
        // 设置是否调用的是dotNet开发的WebService
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);

        HttpTransportSE transport = new HttpTransportSE(webServiceUrl);
        try {
            // 调用WebService
            transport.call(Configure.NAME_SPACE + "/" + Configure.METHOD_NAME, envelope);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 获取返回的数据
        SoapObject object = (SoapObject) envelope.bodyIn;
        // 获取返回的结果
        if (object != null) {
            final String result = object.getProperty(0).toString();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mUploadResult.setText(String.format(getString(R.string.upload_result), result));
                }
            });
            m_myHandler.sendEmptyMessageDelayed(SEND_5,2*1000);
            ToastUtils.showMessage(result);
            return;
        } else {
            ToastUtils.showMessage(R.string.upload_failed);
        }
       /* runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onBackPressed();
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
//        if (mConnected) {
//            menu.findItem(R.id.menu_connect).setVisible(false);
//            menu.findItem(R.id.menu_disconnect).setVisible(true);
//        } else {
//            menu.findItem(R.id.menu_connect).setVisible(true);
//            menu.findItem(R.id.menu_disconnect).setVisible(false);
//        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_restart:
                selectMode = 2;
                App.getInstance().getIBLE().setMode(2);
                return true;
            case R.id.menu_gps_test:
                Intent intent = new Intent(LockManageActivity.this, GpsMainActivity.class);
                startActivity(intent);
                return true;

//            case R.id.menu_connect:
//                App.getInstance().getIBLE().connect(mAddress, this);
//                return true;
//            case R.id.menu_disconnect:
//                App.getInstance().getIBLE().disconnect();
//                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
