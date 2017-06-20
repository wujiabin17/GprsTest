package com.sunshine.blelibrary.mode;

/**
 * Created by yuanbin.ning on 2017/6/6.
 */

public class GetGSMIdTxOrder extends TxOrder {

    public GetGSMIdTxOrder() {
        super(TYPE.GET_GSM_ID);
        add(new byte[]{0x01, 0x00});
    }
}
