package com.sprocomm.gprstest.utils;

import com.sprocomm.gprstest.bean.BleDevice;

import java.util.Comparator;

/**
 * Created by yuanbin.ning on 2017/6/5.
 */

public class SortComparator implements Comparator {
    @Override
    public int compare(Object o, Object t1) {
        BleDevice a = (BleDevice) o;
        BleDevice b = (BleDevice) t1;
        return (b.getRiss() - a.getRiss());
    }
}