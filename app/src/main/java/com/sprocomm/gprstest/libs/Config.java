package com.sprocomm.gprstest.libs;


public class Config
{
    /** 自动对焦 */
    public static final boolean AOTO_FOCUS = true;
    
    /** 前置灯光 */
    public static boolean KEY_FRONT_LIGHT = false;
    
    /** 摄像头配置 Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE in 4.0+ */
    public static final boolean KEY_DISABLE_CONTINUOUS_FOCUS = true;
    
    /** 解码器 */
    public static final boolean KEY_DECODE_1D = false;
    public static final boolean KEY_DECODE_QR = false;
    public static final boolean KEY_DECODE_DATA_MATRIX = false;
    
    /** 扫描到结果后是否震动，默认为false,声音默认为true */
    public static final boolean KEY_PLAY_BEEP = false;
    public static final boolean KEY_VIBRATE = true;
    public static final String NAME_SPACE = "Mes.Web.Services";
    public static final String WEB_SERVICE_URL = "192.168.8.13:82";
    public static final String WEB_SERVICE_URL_TEST = "192.168.0.42:82";
}
