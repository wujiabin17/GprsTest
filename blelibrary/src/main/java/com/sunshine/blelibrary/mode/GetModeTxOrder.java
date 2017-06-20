package com.sunshine.blelibrary.mode;

/**
 * Created by yuanbin.ning on 2017/6/6.
 */

public class GetModeTxOrder extends TxOrder {

    public GetModeTxOrder() {
        super(TYPE.GET_MODE);
        add(new byte[]{0x01, 0x00});
    }
}
