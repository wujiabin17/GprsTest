package com.sprocomm.gprstest.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yuanbin.ning on 2017/6/6.
 */

public class SettingsCase {

    private String name;
    private String value;
    /**
     *       可编辑  测试
     * 0x00:   0     0
     * 0x01:   0     1
     * 0x10:   1     0
     * 0x11:   1     1
     */
    private int state;
    private Line line;

    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String STATE = "state";

    public SettingsCase() {
        line = new Line();
    }

    public SettingsCase(JSONObject jsonObject) {
        this();
        try {
            setName(jsonObject.getString(NAME));
            setValue(jsonObject.getString(VALUE));
            setState(jsonObject.getInt(STATE));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
        line.setText(value);
    }
    public void setLineNum(int num) {
        line.setNum(num);
    }
    public void setTest(boolean isTest) {
        if (isTest) {
            state |= 0x01;
        } else {
            state &= 0x10;
        }
    }
    public boolean isEnable() {
        return (state & 0x10) != 0;
    }
    public boolean isTest() {
        return (state & 0x01) != 0;
    }
    public boolean isFocus() {
        return line.isFocus();
    }
    public void setFocus(boolean focus) {
        line.setFocus(focus);
    }
    public void setState(int state) {
        this.state = state;
    }

    public JSONObject toJSONString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(NAME, name);
            jsonObject.put(VALUE, value);
            jsonObject.put(STATE, state);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
