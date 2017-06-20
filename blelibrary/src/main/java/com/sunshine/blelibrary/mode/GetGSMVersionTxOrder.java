package com.sunshine.blelibrary.mode;

/**
 * Created by yuanbin.ning on 2017/6/6.
 */

public class GetGSMVersionTxOrder extends TxOrder {

    public GetGSMVersionTxOrder() {
        super(TYPE.GET_GSM_VERSION);
        add(new byte[]{0x01, 0x00});
    }
}