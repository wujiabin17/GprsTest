package com.sunshine.blelibrary.mode;

/**
 * Created by yuanbin.ning on 2017/6/6.
 */

public class GetWorkStatusTxOrder extends TxOrder
{
    public GetWorkStatusTxOrder()
    {
        super(TYPE.GET_WORK_STATUS);
        add(new byte[] { 0x01, 0x00 });
    }
}