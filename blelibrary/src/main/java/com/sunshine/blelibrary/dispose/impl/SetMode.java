package com.sunshine.blelibrary.dispose.impl;

import com.sunshine.blelibrary.config.Config;
import com.sunshine.blelibrary.dispose.BaseHandler;
import com.sunshine.blelibrary.utils.GlobalParameterUtils;

/**
 * 作者: Sunshine
 * 时间: 2017/5/6.
 * 邮箱: 44493547@qq.com
 * 描述:
 */

public class SetMode extends BaseHandler {
    @Override
    protected void handler(String hexString,int state) {
        if (hexString.startsWith("05210101")) {
            GlobalParameterUtils.getInstance().sendBroadcast(Config.SET_MODE, "");
        } else {
            GlobalParameterUtils.getInstance().sendBroadcast(Config.SET_MODE, hexString);
        }
    }

    @Override
    protected String action() {
        return "0521";
    }
}

