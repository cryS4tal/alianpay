package com.ylli.api.third.pay.model;

import java.util.List;

public class CntRes {
    public String resultCode;
    public String resultMsg;
    public String date;
    public String orderId;
    public String totalPrice;
    public String referenceCode;
    public List<CntCard> pays;
}
