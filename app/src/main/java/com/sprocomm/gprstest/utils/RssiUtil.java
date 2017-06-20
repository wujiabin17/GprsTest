package com.sprocomm.gprstest.utils;

/**
 * 功能：根据rssi计算距离
 * Created by yuanbin.ning on 2017/6/9.
 */
public class RssiUtil {
    //A和n的值，需要根据实际环境进行检测得出
    /**
     * A - 发射端和接收端相隔1米时的信号强度
     */
    private static final double A_Value = 50;
    /**
     * n - 环境衰减因子
     */
    private static final double n_Value = 2.5;

    /**
     * 根据Rssi获得返回的距离,返回数据单位为m
     *
     * @param rssi
     * @return
     */
    public static double getDistance(int rssi) {
        int iRssi = Math.abs(rssi);
        double power = (iRssi - A_Value) / (10 * n_Value);
        return Math.pow(10, power);
    }
}
