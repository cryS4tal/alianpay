package com.ylli.api.third.pay.model;

import java.util.List;

public class CntRes {

    public static final Integer ZFB_PAY = 0;//支付宝
    public String resultCode;
    public String resultMsg;
    public DataMsg data;

    public class DataMsg {
        public String orderId;
        public String cardId;
        public String totalPrice;
        public String referenceCode;
        public List<CntCard> pays;
    }
}
