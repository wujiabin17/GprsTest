package com.sunshine.blelibrary.impl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.fitsleep.sunshinelibrary.utils.ConvertUtils;
import com.fitsleep.sunshinelibrary.utils.EncryptUtils;
import com.fitsleep.sunshinelibrary.utils.Logger;
import com.sunshine.blelibrary.config.Config;
import com.sunshine.blelibrary.dispose.impl.AQ;
import com.sunshine.blelibrary.dispose.impl.Battery;
import com.sunshine.blelibrary.dispose.impl.CloseLock;
import com.sunshine.blelibrary.dispose.impl.GetDomain;
import com.sunshine.blelibrary.dispose.impl.GetDomain2;
import com.sunshine.blelibrary.dispose.impl.GetGSMId;
import com.sunshine.blelibrary.dispose.impl.GetGSMVersion;
import com.sunshine.blelibrary.dispose.impl.GetICCID;
import com.sunshine.blelibrary.dispose.impl.GetIMEI;
import com.sunshine.blelibrary.dispose.impl.GetIP;
import com.sunshine.blelibrary.dispose.impl.GetMode;
import com.sunshine.blelibrary.dispose.impl.GetWorkStatus;
import com.sunshine.blelibrary.dispose.impl.LockResult;
import com.sunshine.blelibrary.dispose.impl.LockStatus;
import com.sunshine.blelibrary.dispose.impl.OpenLock;
import com.sunshine.blelibrary.dispose.impl.Password;
import com.sunshine.blelibrary.dispose.impl.SetMode;
import com.sunshine.blelibrary.dispose.impl.TY;
import com.sunshine.blelibrary.dispose.impl.Token;
import com.sunshine.blelibrary.dispose.impl.UpdateVersion;
import com.sunshine.blelibrary.inter.IBLE;
import com.sunshine.blelibrary.inter.OnConnectionListener;
import com.sunshine.blelibrary.inter.OnDeviceSearchListener;
import com.sunshine.blelibrary.mode.BatteryTxOrder;
import com.sunshine.blelibrary.mode.GetDomainTxOrder;
import com.sunshine.blelibrary.mode.GetGSMIdTxOrder;
import com.sunshine.blelibrary.mode.GetGSMVersionTxOrder;
import com.sunshine.blelibrary.mode.GetICCIDTxOrder;
import com.sunshine.blelibrary.mode.GetIMEITxOrder;
import com.sunshine.blelibrary.mode.GetIPTxOrder;
import com.sunshine.blelibrary.mode.GetLockStatusTxOrder;
import com.sunshine.blelibrary.mode.GetModeTxOrder;
import com.sunshine.blelibrary.mode.GetTokenTxOrder;
import com.sunshine.blelibrary.mode.GetWorkStatusTxOrder;
import com.sunshine.blelibrary.mode.OpenLockTxOrder;
import com.sunshine.blelibrary.mode.ResetAQTxOrder;
import com.sunshine.blelibrary.mode.ResetLockTxOrder;
import com.sunshine.blelibrary.mode.SetModeTxOrder;
import com.sunshine.blelibrary.mode.TxOrder;
import com.sunshine.blelibrary.service.BLEService;
import com.sunshine.blelibrary.utils.GlobalParameterUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.fitsleep.sunshinelibrary.utils.EncryptUtils.Encrypt;

/**
 * 作者：LiZhao
 * 时间：2017.2.8 11:48
 * 邮箱：44493547@qq.com
 * 备注：
 */
public class AndroidBle implements IBLE {

    private static final String TAG = AndroidBle.class.getSimpleName();
    private BLEService mBLEService;
    private final BluetoothAdapter mBluetoothAdapter;
    private OnDeviceSearchListener mOnDeviceSearchListener;
    private BluetoothGatt mBluetoothGatt;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private OnConnectionListener mOnConnectionListener;
    private BluetoothGattCharacteristic read_characteristic;
    private BluetoothGattCharacteristic write_characteristic;
    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private Token mToken;

    private BluetoothGattCharacteristic OAD_READ;
    private BluetoothGattCharacteristic OAD_WRITE;

    public AndroidBle(BLEService bleService) {
        this.mBLEService = bleService;
        final BluetoothManager bluetoothManager = (BluetoothManager) mBLEService.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return;
        }
        GlobalParameterUtils.getInstance().setContext(mBLEService.getApplicationContext());
        mToken = new Token();
        GetGSMId getGSMId = new GetGSMId();
        GetMode getMode = new GetMode();
        GetWorkStatus getWorkStatus = new GetWorkStatus();
        GetGSMVersion getGSMVersion = new GetGSMVersion();
        GetICCID getICCID = new GetICCID();
        GetIMEI getIMEI = new GetIMEI();
        GetDomain getDomain = new GetDomain();
        GetDomain2 getDomain2 = new GetDomain2();
        GetIP getIP = new GetIP();
        Battery battery = new Battery();
        OpenLock openLock = new OpenLock();
        TY ty = new TY();
        CloseLock closeLock = new CloseLock();
        LockStatus lockStatus = new LockStatus();
        Password password = new Password();
        LockResult lockResult = new LockResult();
        AQ aq = new AQ();
        UpdateVersion updateVersion = new UpdateVersion();
        SetMode setMode = new SetMode();

        mToken.nextHandler = getGSMId;
        getGSMId.nextHandler = getMode;
        getMode.nextHandler = getWorkStatus;
        getWorkStatus.nextHandler = getGSMVersion;
        getGSMVersion.nextHandler = getICCID;
        getICCID.nextHandler = getIMEI;
        getIMEI.nextHandler = getDomain;
        getDomain.nextHandler = getDomain2;
        getDomain2.nextHandler = getIP;

        getIP.nextHandler = battery;
        battery.nextHandler = openLock;
        openLock.nextHandler = ty;
        ty.nextHandler = closeLock;
        closeLock.nextHandler = lockStatus;
        lockStatus.nextHandler = password;
        password.nextHandler = lockResult;
        lockResult.nextHandler = aq;
        aq.nextHandler = updateVersion;
        updateVersion.nextHandler = setMode;
    }

    @Override
    public void setDebug(boolean isOpen) {

    }

    @Override
    public void startScan(OnDeviceSearchListener onDeviceSearchListener) {
        bluetoothDeviceList.clear();
        if (mBluetoothAdapter == null) return;
        mBluetoothAdapter.cancelDiscovery();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        mBluetoothAdapter.startDiscovery();
        this.mOnDeviceSearchListener = onDeviceSearchListener;
        mBluetoothAdapter.startLeScan(new UUID[]{Config.bltServerUUID}, mLeScanCallback);
    }

    @Override
    public void stopScan() {
        if (mBluetoothAdapter == null) return;
        mBluetoothAdapter.cancelDiscovery();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    @Override
    public void connect(String address, OnConnectionListener onConnectionListener) {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
        if (null == onConnectionListener) return;
        this.mOnConnectionListener = onConnectionListener;

        if (TextUtils.isEmpty(address) || mBluetoothAdapter == null) {
            mOnConnectionListener.onDisconnect(Config.OBJECT_EMPTY);
            return;
        }

        BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (null == bluetoothDevice) {
            mOnConnectionListener.onDisconnect(Config.OBJECT_EMPTY);
            return;
        }

        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        mBluetoothGatt = bluetoothDevice.connectGatt(mBLEService, false, mBluetoothGattCallback);
    }

    @Override
    public void writeByte(byte[] bytes) {
        if (mBluetoothGatt == null || write_characteristic == null) {
            return;
        }

        byte[] miwen = null;
        switch (GlobalParameterUtils.getInstance().getLockType()) {
            case MTS:
                miwen = Encrypt(bytes, Config.key);
                break;
            case YXS:
                miwen = Encrypt(bytes, Config.yx_key);
                break;
        }
        if (miwen != null) {
            write_characteristic.setValue(miwen);
            mBluetoothGatt.writeCharacteristic(write_characteristic);
            Logger.e(AndroidBle.class.getSimpleName(), ConvertUtils.bytes2HexString(bytes));
        }

    }

    @Override
    public boolean getToken() {
        return writeObject(new GetTokenTxOrder());
    }

    @Override
    public boolean getBattery() {
        return writeObject(new BatteryTxOrder());
    }

    @Override
    public boolean openLock() {
        return writeObject(new OpenLockTxOrder());
    }

    @Override
    public boolean resetLock() {
        return writeObject(new ResetLockTxOrder());
    }

    @Override
    public boolean getLockStatus() {
        return writeObject(new GetLockStatusTxOrder());
    }

    @Override
    public boolean setPassword() {
        return false;
    }

    @Override
    public boolean setKey() {
        return false;
    }

    @Override
    public boolean updateVersion() {
        return false;
    }

    @Override
    public boolean writeWrite(byte[] bytes) {
        if (mBluetoothGatt == null || OAD_WRITE == null) {
            return false;
        }
        OAD_WRITE.setValue(bytes);
        return mBluetoothGatt.writeCharacteristic(OAD_WRITE);
    }

    @Override
    public boolean writeRead(byte[] bytes) {
        if (mBluetoothGatt == null || OAD_READ == null) {
            return false;
        }
        OAD_READ.setValue(bytes);
        return mBluetoothGatt.writeCharacteristic(OAD_READ);
    }

    @Override
    public boolean setMode(int position) {
        return writeObject(new SetModeTxOrder(position));
    }


    @Override
    public void resetPasswordAndAQ() {
        writeObject(new ResetAQTxOrder());
    }

    @Override
    public void disconnect() {
        if (mBluetoothGatt == null) {
            return;
        }
        refreshDeviceCache();
        mBluetoothGatt.disconnect();

    }

    @Override
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        refreshDeviceCache();
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    @Override
    public void resetBluetoothAdapter() {
        if (mBluetoothAdapter==null)return;
        mBluetoothAdapter.disable();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
               mBluetoothAdapter.enable();
            }
        },200);
    }

    @Override
    public boolean getMode() {
        return writeObject(new GetModeTxOrder());
    }

    @Override
    public boolean getGSMId() {
        return writeObject(new GetGSMIdTxOrder());
    }

    @Override
    public boolean getGSMVersion() {
        return writeObject(new GetGSMVersionTxOrder());
    }

    @Override
    public boolean getICCID() {
        return writeObject(new GetICCIDTxOrder());
    }

    @Override
    public boolean getIMEI() {
        return writeObject(new GetIMEITxOrder());
    }

    @Override
    public boolean getDomain() {
        return writeObject(new GetDomainTxOrder());
    }

    @Override
    public boolean getIP() {
        return writeObject(new GetIPTxOrder());
    }

    @Override
    public boolean getWorkStatus() {
        return writeObject(new GetWorkStatusTxOrder());
    }

    @Override
    public void bluetoothEnable(){
        mBluetoothAdapter.enable();
    }


    /**
     * 写入指令
     *
     * @param txOrder 发送指令对象
     * @return 是否成功
     */
    private boolean writeObject(TxOrder txOrder) {
        if (mBluetoothGatt == null || write_characteristic == null) {
            return false;
        }

        byte[] miwen = null;
        switch (GlobalParameterUtils.getInstance().getLockType()) {
            case MTS:
                miwen = Encrypt(ConvertUtils.hexString2Bytes(txOrder.generateString()), Config.key);
                break;
            case YXS:
                miwen = Encrypt(ConvertUtils.hexString2Bytes(txOrder.generateString()), Config.yx_key);
                break;
        }
        if (miwen != null) {
            write_characteristic.setValue(miwen);
            Logger.e("发送：", txOrder.generateString());
            return mBluetoothGatt.writeCharacteristic(write_characteristic);
        }
        return false;
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            if (null != mOnDeviceSearchListener) {
                mOnDeviceSearchListener.onScanDevice(device, rssi, scanRecord);
            }
        }
    };

    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnect();
            }
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    gatt.discoverServices();
                    if (null != mOnConnectionListener) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mOnConnectionListener.onConnect();
                            }
                        });
                    }
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Logger.e(AndroidBle.class.getSimpleName(),"断开");
                    if (null != mOnConnectionListener) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mOnConnectionListener.onDisconnect(Config.DISCONNECT);
                            }
                        });
                    }
                    gatt.close();
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (GlobalParameterUtils.getInstance().isUpdate()) {
                    BluetoothGattService service = gatt.getService(Config.OAD_SERVICE_UUID);
                    if (null != service) {
                        OAD_READ = service.getCharacteristic(Config.OAD_READ_UUID);
                        OAD_WRITE = service.getCharacteristic(Config.OAD_WRITE_UUID);
                        OAD_WRITE.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    }
                } else {
                    BluetoothGattService service = gatt.getService(Config.bltServerUUID);
                    if (null != service) {
                        read_characteristic = service.getCharacteristic(Config.readDataUUID);
                        write_characteristic = service.getCharacteristic(Config.writeDataUUID);
                        int properties = read_characteristic.getProperties();
                        if ((properties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            gatt.setCharacteristicNotification(read_characteristic, true);
                            BluetoothGattDescriptor descriptor = read_characteristic.getDescriptor(Config.CLIENT_CHARACTERISTIC_CONFIG);
                            if (null != descriptor) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(descriptor);
                            }
                        }
                    }
                }
                if (null != mOnConnectionListener) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mOnConnectionListener.onServicesDiscovered(TextUtils.isEmpty(gatt.getDevice().getName())?"null":gatt.getDevice().getName(), gatt.getDevice().getAddress());
                        }
                    });
                }
            }
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            GlobalParameterUtils.getInstance().setBusy(false);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            GlobalParameterUtils.getInstance().setBusy(false);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (GlobalParameterUtils.getInstance().isUpdate()) {
                GlobalParameterUtils.getInstance().setBusy(false);
//                mBLEService.updateBroadcast(ConvertUtils.bytes2HexString(characteristic.getValue()));
                Logger.e(TAG, "onCharacteristicChanged:" + ConvertUtils.bytes2HexString(characteristic.getValue()));
            } else {
                try {
                    byte[] values = characteristic.getValue();
                    byte[] x = new byte[16];
                    System.arraycopy(values, 0, x, 0, 16);

                    byte mingwen[] = null;
                    switch (GlobalParameterUtils.getInstance().getLockType()) {
                        case MTS:
                            mingwen = EncryptUtils.Decrypt(x, Config.key);
                            break;
                        case YXS:
                            mingwen = EncryptUtils.Decrypt(x, Config.yx_key);
                            break;
                    }
                    Logger.e(TAG, "返回：" + ConvertUtils.bytes2HexString(mingwen));
                    mToken.handlerRequest(ConvertUtils.bytes2HexString(mingwen), 0);
                } catch (Exception e) {
                    byte[] values = characteristic.getValue();
                    byte[] x = new byte[16];
                    System.arraycopy(values, 0, x, 0, 16);
                    byte mingwen[] = EncryptUtils.Decrypt(x, Config.key);
                    Logger.e(TAG, "没有该指令：" + ConvertUtils.bytes2HexString(mingwen));
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            GlobalParameterUtils.getInstance().setBusy(false);
        }
    };

    public boolean refreshDeviceCache(){
        if (mBluetoothGatt != null){
            try {
                BluetoothGatt localBluetoothGatt = mBluetoothGatt;
                Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
                if (localMethod != null) {
                    boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                    return bool;
                }
            }
            catch (Exception localException) {
                Logger.e("#############","An exception occured while refreshing device");
            }
        }
        return false;
    }
}
