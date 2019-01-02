package com.ylli.api.third.pay.model;

import java.util.List;

public class CntRes {
    public static final Integer CNT_BUY = 1;//下单
    public static final Integer CNT_CASH = 0;//体现
    public static final Integer ZFB_PAY = 0;//支付宝
    public static final Integer WX_PAY = 1;//微信
    public String resultCode;
    public String resultMsg;
    public DataMsg data;

    public class DataMsg {
        public String orderId;
        public String totalPrice;
        public String referenceCode;
        public List<CntCard> pays;
    }
}
