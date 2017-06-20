package com.sprocomm.gprstest.utils;

import android.util.Log;

import com.sprocomm.gprstest.config.Configure;

/**
 * Created by yuanbin.ning on 2017/6/5.
 */

public class LogUtils
{
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int NOTHING = 6;

    public static void v(String tag, String msg) {
        if (Configure.LOG_LEVEL <= VERBOSE) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (Configure.LOG_LEVEL <= DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (Configure.LOG_LEVEL <= INFO) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (Configure.LOG_LEVEL <= WARN) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (Configure.LOG_LEVEL <= ERROR) {
            Log.e(tag, msg);
        }
    }
}
