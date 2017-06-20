package com.sunshine.blelibrary.dispose.impl;

import com.sunshine.blelibrary.config.Config;
import com.sunshine.blelibrary.dispose.BaseHandler;
import com.sunshine.blelibrary.utils.GlobalParameterUtils;

/**
 * 固件升级
 * Created by sunshine on 2017/2/21.
 */

public class UpdateVersion extends BaseHandler {
    @Override
    protected void handler(String hexString, int state) {
//        byte[] mingwen = ConvertUtils.hexString2Bytes(hexString);
//        Logger.e("UpdateVersion","decrypt:"+ ConvertUtils.bytes2HexString(mingwen));
//        Intent intent = new Intent();
//        intent.setAction(Config.UPDATE_VERSION_ACTION);
        if (hexString.startsWith("03020101")){
            GlobalParameterUtils.getInstance().sendBroadcast(Config.UPDATE_VERSION_ACTION,"");
        }else {
//            intent.putExtra("data",ConvertUtils.bytes2HexString(mingwen));
            GlobalParameterUtils.getInstance().sendBroadcast(Config.UPDATE_VERSION_ACTION,hexString);
        }
//        GlobalParameterUtils.getInstance().getContext().sendBroadcast(intent);
    }

    @Override
    protected String action() {
        return "030201";
    }
}
