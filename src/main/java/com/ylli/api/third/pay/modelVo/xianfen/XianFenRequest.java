package com.ylli.api.third.pay.modelVo.xianfen;

// 单笔代发 & 线下充值
public class XianFenRequest {

    //商户订单号
    public String merchantNo;

    //金额/分
    public Integer amount;

    //币种_固定值：156（表示人民币）
    public String transCur;

    //用户类型（1：对私 2：对公）
    public Integer userType;

    //提现卡号
    public String accountNo;

    //持卡人姓名(对公代发姓名字段必须6个汉字以上)
    public String accountName;

    /**
     * 1（借记卡）
     * 2（贷记卡）
     * 4（对公账户）
     * not required.
     *
     *  userType = 1; accountType = (1,2) 默认1
     *  userType = 2; accountType 默认4
     */
    public Integer accountType;

    //手机号 not required
    public String mobileNo;

    //银行编码
    public String bankNo;

    /**
     * 联行号
     * userType = 2 required
     */
    public String issuer;

    //后台通知地址
    public String noticeUrl;

    //保留域_原样回传
    public String memo;

    //备付金账户标识 UPOPJS（银联）NUCC（网联）
    public String recevieBank;

    public XianFenRequest() {
    }
}
