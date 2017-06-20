package com.sprocomm.gprstest.bean;

/**
 * Created by yuanbin.ning on 2017/6/6.
 */

public abstract class TestCase {
    public int type;

    public static final int TYPE_1 = 0;
    public static final int TYPE_2 = 1;

    private String name;
    private String value;
    private boolean isPass;
    private int state; // 0:不显示Pass/Fail,1:显示Pass/Fail

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
    }
    public boolean isPass() {
        return isPass;
    }
    public void setPass(boolean pass) {
        this.isPass = pass;
    }
    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
    }
}
