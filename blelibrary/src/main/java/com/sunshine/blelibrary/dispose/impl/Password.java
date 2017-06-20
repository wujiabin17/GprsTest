package com.sunshine.blelibrary.dispose.impl;

import com.sunshine.blelibrary.config.Config;
import com.sunshine.blelibrary.dispose.BaseHandler;
import com.sunshine.blelibrary.utils.GlobalParameterUtils;

/**
 * 修改密码
 * Created by sunshine on 2017/2/20.
 */

public class Password extends BaseHandler {
    @Override
    protected void handler(String hexString, int state) {
        if (hexString.startsWith("05050101")){
            GlobalParameterUtils.getInstance().sendBroadcast(Config.PASSWORD_ACTION,"");
        }else {
            GlobalParameterUtils.getInstance().sendBroadcast(Config.PASSWORD_ACTION,hexString);
        }
    }

    @Override
    protected String action() {
        return "0505";
    }
}
