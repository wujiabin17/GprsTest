package com.sunshine.blelibrary.mode;

/**
 * Created by yuanbin.ning on 2017/6/6.
 */

public class GetIMEITxOrder extends TxOrder {

    public GetIMEITxOrder() {
        super(TYPE.GET_IMEI);
        add(new byte[]{0x01, 0x00});
    }
}
