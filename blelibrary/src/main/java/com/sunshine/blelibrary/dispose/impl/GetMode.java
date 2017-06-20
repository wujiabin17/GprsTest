package com.sunshine.blelibrary.dispose.impl;

import com.fitsleep.sunshinelibrary.utils.ConvertUtils;
import com.sunshine.blelibrary.config.Config;
import com.sunshine.blelibrary.dispose.BaseHandler;
import com.sunshine.blelibrary.utils.GlobalParameterUtils;

/**
 * 电量
 * Created by sunshine on 2017/2/20.
 */

public class GetMode extends BaseHandler {
    @Override
    protected void handler(String hexString, int state) {

        if (hexString.startsWith("05200101"))
        {
            GlobalParameterUtils.getInstance().sendBroadcast(Config.GET_MODE, "");
            return;
        }
        GlobalParameterUtils.getInstance().sendBroadcast(Config.GET_MODE, hexString);
    }

    @Override
    protected String action() {
        return "0520";
    }
}
