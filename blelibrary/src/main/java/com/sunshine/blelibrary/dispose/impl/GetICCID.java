package com.sunshine.blelibrary.dispose.impl;

import com.fitsleep.sunshinelibrary.utils.ConvertUtils;
import com.sunshine.blelibrary.config.Config;
import com.sunshine.blelibrary.dispose.BaseHandler;
import com.sunshine.blelibrary.utils.GlobalParameterUtils;

/**
 * 电量
 * Created by sunshine on 2017/2/20.
 */

public class GetICCID extends BaseHandler {
    @Override
    protected void handler(String hexString, int state) {
        String str = hexString.substring(6, 6 + 2 * ConvertUtils.hexString2Bytes(hexString)[2]);
        GlobalParameterUtils.getInstance().sendBroadcast(Config.GET_ICCID_ACTION, str);
    }

    @Override
    protected String action() {
        return "0528";
    }
}
