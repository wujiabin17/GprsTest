package com.sprocomm.gprstest.config;

import com.sprocomm.gprstest.utils.LogUtils;

/**
 * Created by yuanbin.ning on 2017/6/5.
 */

public class Configure {
    public static final int LOG_LEVEL = LogUtils.VERBOSE; // 打印日志级别

    public static final byte[] KEY = { 32, 87, 47, 82, 54, 75, 63, 71, 48, 80,
            65, 88, 17, 99, 45, 43 };

    public static final byte[] PWD = { 0x30, 0x30, 0x30, 0x30, 0x30, 0x30 };

    public static final String NAME_SPACE = "Mes.Web.Services";
    public static final String METHOD_SUB_SEMI_BT_TEST = "SubSemiFinishedBTTest";
    public static final String METHOD_SUB_BT_TEST = "SubFinishedBTTest";

    public static String SERVER_IP = "192.168.8.13";
    public static String METHOD_NAME = METHOD_SUB_SEMI_BT_TEST;

    public static final String PASSWORD = "654321";
}
