package com.sunshine.blelibrary.mode;

/**
 * Created by yuanbin.ning on 2017/6/6.
 */

public class GetDomainTxOrder extends TxOrder {

    public GetDomainTxOrder() {
        super(TYPE.GET_DOMAIN);
        add(new byte[]{0x01, 0x00});
    }
}
