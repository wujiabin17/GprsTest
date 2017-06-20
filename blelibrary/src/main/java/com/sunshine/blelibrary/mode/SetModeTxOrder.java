package com.sunshine.blelibrary.mode;

/**
 * 作者: Sunshine
 * 时间: 2017/5/6.
 * 邮箱: 44493547@qq.com
 * 描述:
 */

public class SetModeTxOrder extends TxOrder {
    /**
     * @param type 指令类型
     */
    public SetModeTxOrder(int type) {
        super(TYPE.SET_MODE);
        if (type==0){
            add(new byte[]{0x01,0x00});
        }else if (type==1){
            add(new byte[]{0x01,0x01});
        }else if (type==2){
            add(new byte[]{0x01,0x02});
        }
    }
}
