package com.ylli.api.third.pay.modelVo.cntbnt;

import java.util.List;

public class CntRes {

    public String resultCode;
    public String resultMsg;
    public DataMsg data;

    public class DataMsg {
        public String orderId;
        public String cardId;
        public String totalPrice;
        public String referenceCode;
        public List<CNTCard> pays;
    }
}
