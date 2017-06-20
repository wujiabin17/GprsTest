package com.sprocomm.gprstest.activity;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fitsleep.sunshinelibrary.utils.IntentUtils;
import com.fitsleep.sunshinelibrary.utils.Logger;
import com.fitsleep.sunshinelibrary.utils.MPermissionsActivity;
import com.fitsleep.sunshinelibrary.utils.ToolsUtils;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.sprocomm.gprstest.activity.Distance;
import com.sprocomm.gprstest.activity.LockManageActivity;
import com.sprocomm.gprstest.activity.SettingsActivity;
import com.sprocomm.gprstest.application.App;
import com.sprocomm.gprstest.bean.BleDevice;
import com.sprocomm.gprstest.config.Configure;
import com.sprocomm.gprstest.utils.LogUtils;
import com.sprocomm.gprstest.utils.ParseLeAdvData;
import com.sprocomm.gprstest.utils.SortComparator;
import com.sprocomm.gprstest.utils.Utils;
import com.sprocomm.gprstest.R;
import com.sunshine.blelibrary.inter.OnDeviceSearchListener;
import com.sunshine.blelibrary.utils.GlobalParameterUtils;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BlueMainActivity extends MPermissionsActivity implements OnDeviceSearchListener, View.OnClickListener {

    TextView tvScan;
    Button btScan;
    ListView listItem;
    TextView appVersion;

    private boolean isScan = true;
    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private List<BleDevice> bleDeviceList = new ArrayList<>();
    private ListAdapter mAdapter;
    private List<BleDevice> adapterList = new ArrayList<>();
    private BleDevice bleDevice;

    private ParseLeAdvData parseLeAdvData;
    private Comparator comp;
    private Button btnSearchSettings;
    private Button btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blue_activity_main);
        tvScan = (TextView) findViewById(R.id.tv_scan);
        btScan = (Button) findViewById(R.id.bt_scan);
        listItem = (ListView) findViewById(R.id.list_item) ;
        appVersion = (TextView) findViewById(R.id.app_version) ;
        btnSearchSettings = (Button)findViewById(R.id.search_settings);
        btnSettings = (Button) findViewById(R.id.testcase_settings);
        btnSettings.setOnClickListener(this);
        btnSearchSettings.setOnClickListener(this);
        btScan.setOnClickListener(this);
        init();
    }

    private void init() {
        parseLeAdvData = new ParseLeAdvData();
        comp = new SortComparator();
        appVersion.setText("Version:" + ToolsUtils.getVersion(getApplicationContext()));
        mAdapter = new ListAdapter();
        listItem.setAdapter(mAdapter);
        listItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                App.getInstance().getIBLE().stopScan();
                BleDevice bluetoothDevice = adapterList.get(position);
                String name = bluetoothDevice.getDevice().getName();
                if (TextUtils.isEmpty(name)) {
                    name = "null";
                }
                String address = bluetoothDevice.getDevice().getAddress();
                if (name.equals("NokeLockOAD")) {
                    GlobalParameterUtils.getInstance().setUpdate(true);
                } else {
                    GlobalParameterUtils.getInstance().setUpdate(false);
                }
                Bundle bundle = new Bundle();
                bundle.putString("name", name);
                bundle.putString("address", address);
                IntentUtils.startActivity(BlueMainActivity.this, LockManageActivity.class, bundle);
            }
        });
        new Thread(new DeviceThread()).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scanDevice();
            }
        }, 500);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.search_settings:
                searchSettings();
                break;
            case R.id.bt_scan:
                scanDevice();
                break;
            case R.id.testcase_settings:
                testcaseSettings();
                break;
        }
    }

    void scanDevice() {
        if (isScan) {
            requestPermission(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        }
    }

    void testcaseSettings() {
        createDialog();
    }

    void searchSettings() {
        Intent intent = new Intent(BlueMainActivity.this, Distance.class);
        intent.putExtra("value", rissValue);
        startActivityForResult(intent, 0);
    }

    private int rissValue;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0:
                    String rissSet = data.getStringExtra("rissValue");
                    try {
                        int value = Integer.parseInt(rissSet);
                        if (value > 0) {
                            rissValue = 0 - value;
                        } else {
                            rissValue = value;
                        }
                    } catch (NumberFormatException e) {
                        rissValue = 0;
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void permissionSuccess(int requestCode) {
        super.permissionSuccess(requestCode);
        if (101 == requestCode) {
            LogUtils.e(getClass().getSimpleName(), "申请成功了");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tvScan.setText("扫描结束");
                    isScan = true;
                    App.getInstance().getIBLE().stopScan();
                }
            }, 5000);
            tvScan.setText("开始扫描...");
            isScan = false;
            bluetoothDeviceList.clear();
            adapterList.clear();
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
            App.getInstance().getIBLE().startScan(this);
        }
    }

    @Override
    public void onScanDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (!bluetoothDeviceList.contains(device)) {
            bluetoothDeviceList.add(device);
            bleDevice = new BleDevice(device, scanRecord, rssi);
            bleDeviceList.add(bleDevice);
            Logger.e(BlueMainActivity.class.getSimpleName(), device.getAddress() + "---" + rssi);
        }
    }

    private boolean parseAdvData(int rssi, byte[] scanRecord) {
        if (rissValue != 0 && rssi < rissValue) {
            return false;
        }
        byte[] bytes = ParseLeAdvData.adv_report_parse(ParseLeAdvData.BLE_GAP_AD_TYPE_MANUFACTURER_SPECIFIC_DATA, scanRecord);
        if (null == bytes || bytes.length < 2) {
            return false;
        }
        if (bytes[0] == 0x01 && bytes[1] == 0x02) {
            return true;
        }
        return false;
    }

    private static final int MSG_UPDATE_LIST = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_LIST:
                    Collections.sort(adapterList, comp);
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };


    private class ListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return adapterList.size() > 5 ? 5 : adapterList.size();
        }

        @Override
        public Object getItem(int position) {
            return adapterList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(BlueMainActivity.this, R.layout.item_device, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            BleDevice device = adapterList.get(position);
            viewHolder.listRiss.setText("RISS：" + device.getRiss());
            viewHolder.listAddress.setText(device.getDevice().getName() + "\n" + device.getDevice().getAddress());
            return convertView;
        }

        class ViewHolder {
            TextView listRiss;
            TextView listAddress;

            ViewHolder(View view) {
                listAddress = (TextView) view.findViewById(R.id.list_name);
                listRiss = (TextView) view.findViewById(R.id.list_riss);
            }
        }
    }

    class DeviceThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (bleDeviceList.size() > 0) {
                    BleDevice bleDevice = bleDeviceList.get(0);
                    if (null != bleDevice && parseAdvData(bleDevice.getRiss(), bleDevice.getScanBytes())) {
                        adapterList.add(bleDevice);
                        handler.sendEmptyMessage(MSG_UPDATE_LIST);
                    }
                    bleDeviceList.remove(0);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_settings:
                createDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.dialog_edit,
                null);
        final Dialog dialog = new Dialog(this);
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        window.setContentView(linearLayout);
        window.setBackgroundDrawable(new ColorDrawable(0));
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.width = Utils.dip2px(this, 255);
        window.setGravity(Gravity.CENTER);
        window.setAttributes(lp);
        TextView title = (TextView) linearLayout.findViewById(R.id.dialog_title);
        title.setText("请输入密码：");
        final EditText editText = (EditText) linearLayout.findViewById( R.id.dialog_edittext);
        Button okButton = (Button) linearLayout.findViewById(R.id.id_ok);
        Button cancelButton = (Button) linearLayout.findViewById(R.id.id_cancel);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                if (text.equals(Configure.PASSWORD)) {
                    startActivity(new Intent(BlueMainActivity.this, SettingsActivity.class));
                    dialog.cancel();
                } else {
                    dialog.cancel();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BlueMainActivity.this,
                                    R.string.password_error, Toast.LENGTH_SHORT);
                        }
                    });
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
    }
}
