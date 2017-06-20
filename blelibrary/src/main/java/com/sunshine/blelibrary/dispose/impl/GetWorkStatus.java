package com.sunshine.blelibrary.dispose.impl;

import com.sunshine.blelibrary.config.Config;
import com.sunshine.blelibrary.dispose.BaseHandler;
import com.sunshine.blelibrary.utils.GlobalParameterUtils;

/**
 * 电量
 * Created by sunshine on 2017/2/20.
 */

public class GetWorkStatus extends BaseHandler {
    @Override
    protected void handler(String hexString, int state) {
        GlobalParameterUtils.getInstance().sendBroadcast(Config.GET_WORK_STATUS, hexString);
    }

    @Override
    protected String action() {
        return "0522";
    }
}
