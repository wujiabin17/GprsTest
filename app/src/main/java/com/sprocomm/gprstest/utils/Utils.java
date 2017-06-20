package com.sprocomm.gprstest.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.JsonReader;

import com.sprocomm.gprstest.bean.SettingsCase;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuanbin.ning on 2017/6/7.
 */

public class Utils {

    public static String bcd2Str(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
            temp.append((byte) (bytes[i] & 0x0f));
        }
        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp
                .toString().substring(1) : temp.toString();
    }

    public static String asciiToStr(byte[] value) {
        StringBuffer sbu = new StringBuffer();
        for (int i = 0; i < value.length; i++) {
            sbu.append((char) value[i]);
        }
        return sbu.toString();
    }

    public static boolean compare(int value, int min, int max) {
        if (min == max) {
            if (value == min) {
                return true;
            } else {
                return false;
            }
        } else if (min < max) {
            if (value < min || value > max) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public static boolean compare(int value, String min_max) {
        if (!TextUtils.isEmpty(min_max)) {
            char[] array = min_max.trim().toCharArray();
            int index = 0;
            for (int i = 0; i < array.length; ++i) {
                try {
                    Integer.parseInt(String.valueOf(array[i]));
                } catch (NumberFormatException e) {
                    index = i;
                    break;
                }
            }
            try {
                int min = Integer.parseInt(min_max.trim().substring(0, index));
                int max = Integer.parseInt(min_max.trim().substring(index + 1,
                        array.length));
                return compare(value, min, max);
            } catch (NumberFormatException e) {
            }
        }
        return false;
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        if (dipValue < 0) {
            return (int) (dipValue * scale - 0.5f);
        } else {
            return (int) (dipValue * scale + 0.5f);
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        } else {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null) {
                return networkInfo.isAvailable();
            } else {
                return false;
            }
        }
    }

    public static List<SettingsCase> getDefaultValues(Context context) {
        AssetManager asset = context.getAssets();
        try {
            List<SettingsCase> data = new ArrayList<>();
            int num = 0;
            InputStream input = asset.open("case_default.json");
            JsonReader jsonReader = new JsonReader(new InputStreamReader(input));
            jsonReader.beginArray();
            while(jsonReader.hasNext()) {
                jsonReader.beginObject();
                String name = null;
                String value = null;
                int state = 0;
                while (jsonReader.hasNext()) {
                    String key = jsonReader.nextName();
                    if (key.equals("name")) {
                        name = jsonReader.nextString();
                    } else if (key.equals("value")) {
                        value = jsonReader.nextString();
                    } else if (key.equals("state")) {
                        state = jsonReader.nextInt();
                    } else {
                        jsonReader.skipValue();
                    }
                }
                if (name != null) {
                    SettingsCase sc = new SettingsCase();
                    sc.setName(name);
                    sc.setValue(value);
                    sc.setState(state);
                    sc.setLineNum(num);
                    data.add(sc);
                    num += 1;
                }
                jsonReader.endObject();
            }
            jsonReader.endArray();
            jsonReader.close();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getHexBytes(String message) {
        int len = message.length() / 2;
        char[] chars = message.toCharArray();
        String[] hexStr = new String[len];
        byte[] bytes = new byte[len];
        for (int i = 0, j = 0; j < len; i += 2, j++) {
            hexStr[j] = "" + chars[i] + chars[i + 1];
            bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
        }
        return bytes;
    }
}
