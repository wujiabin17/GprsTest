package com.sunshine.blelibrary.inter;

/**
 * 作者：LiZhao
 * 时间：2017.2.8 11:29
 * 邮箱：44493547@qq.com
 * 备注：BLE操作接口
 */
public interface IBLE {

    /**
     * 开启日志
     * @param isOpen 是否开启
     */
    void setDebug(boolean isOpen);

    /**
     * 扫描设备
     */
    void startScan(OnDeviceSearchListener onDeviceSearchListener);

    /**
     * 停止扫描
     */
    void stopScan();

    /**
     * 链接设备
     * @param address 设备地址
     * @param onConnectionListener 链接状态回调
     */
    void connect(String address,OnConnectionListener onConnectionListener);

    /**
     * 写指令 指令byte
     * @param bytes
     */
    void writeByte(byte[] bytes);

    /**
     * 获取Token
     * @return
     */
    boolean getToken();

    boolean getBattery();

    /**
     * 开锁
     * @return
     */
    boolean openLock();

    /**
     * 复位
     * @return
     */
    boolean resetLock();
    /**
     * 获取锁状态
     * @return
     */
    boolean getLockStatus();

    /**
     * 修改密码
     * @return
     */
    boolean setPassword();

    /**
     * 修改密钥
     * @return
     */
    boolean setKey();

    /**
     * oad模式
     * @return
     */
    boolean updateVersion();
    /**
     * oad升级时写写入的特征值
     * @param bytes
     * @return
     */
    boolean writeWrite(byte[] bytes);

    /**
     * oad升级时写读取的特征值
     * @param bytes
     * @return
     */
    boolean writeRead(byte[] bytes);

    /**
     * 设置工作模式
     * @return
     */
    boolean setMode(int position);

    /**
     * 重置密钥和密码
     */
    void resetPasswordAndAQ();

    void bluetoothEnable();
    /**
     * 断开链接
     */
    void disconnect();

    void close();

    void resetBluetoothAdapter();

    boolean getMode();

    boolean getGSMId();

    boolean getGSMVersion();

    boolean getICCID();

    boolean getIMEI();

    boolean getDomain();

    boolean getIP();

    boolean getWorkStatus();
}
