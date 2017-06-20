package com.sprocomm.gprstest.bean;

/**
 * Created by yuanbin.ning on 2017/6/6.
 */

public class TwoCase extends TestCase {
    private TestCase testCase;
    public TwoCase() {
        this.type = TYPE_2;
    }

    public void setCase(OneCase oc1, OneCase oc2) {
        setName(oc1.getName());
        setValue(oc1.getValue());
        setState(oc1.getState());
        setPass(oc1.isPass());
        this.testCase = oc2;
    }

    public TestCase getTestCase() {
        return testCase;
    }
}
