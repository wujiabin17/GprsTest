package com.sunshine.blelibrary.mode;

/**
 * Created by yuanbin.ning on 2017/6/6.
 */

public class GetIPTxOrder extends TxOrder {

    public GetIPTxOrder() {
        super(TYPE.GET_IP);
        add(new byte[]{0x01, 0x00});
    }
}
