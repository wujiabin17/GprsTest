package com.sunshine.blelibrary.mode;

/**
 * Created by yuanbin.ning on 2017/6/6.
 */

public class GetICCIDTxOrder extends TxOrder {

    public GetICCIDTxOrder() {
        super(TYPE.GET_ICCID);
        add(new byte[]{0x01, 0x00});
    }
}
