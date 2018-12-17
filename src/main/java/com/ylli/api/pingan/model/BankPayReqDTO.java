package com.ylli.api.pingan.model;

import java.io.Serializable;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author 李智鹏
 * @Description: 支付前置DTO
 * @date 2018年3月29日 上午10:50:46
 */
public class BankPayReqDTO extends BaseGatewayReqDTO implements Serializable {
    private static final long serialVersionUID = -5781186303834549893L;

    /**
     * 收款人信息
     **/
    private PayeeInfo payeeInfo;

    /**
     * 付款人信息
     **/
    private PayerInfo payerInfo;

    /**
     * 交易金额
     **/
    private String amount;

    /**
     * 交易描述
     **/
    private String desc;

    /**
     * 交易币种
     **/
    private String currency;

    /**
     * 回显地址
     */
    @NotBlank(message = "回显地址不能为空", groups = {UnBqpGroup.sign.class})
    private String returnUrl;

    /**
     * 回显地址
     */
    @NotBlank(message = "异步通知地址不能为空", groups = {UnBqpGroup.sign.class})
    private String notifyUrl;

    /**
     * 交易超时时间，单位：分钟
     **/
    private String timeoutTime;
    /**
     * 交易验证码
     **/
    private String msgCheckCode;
    /**
     * 交易生成客户端ip
     */
    private String orderCreateIp;
    /**
     * 短信验证码
     */
    private String verCode;
    /**
     * 商品标识
     */
    private String goodsId;
    /**
     * 商品类型
     */
    private String goodsType;
    /**
     * 商品数量
     */
    private Integer goodsQuantity;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 商品价格
     */
    private String goodsPrice;

    /**
     * 手续费
     **/
    private String fee;
    /***分账百分比**/
    private String subAccountRate;
    /***分账账户号**/
    private String subAccountNo;
    /***分账账户名称**/
    private String subAccountName;

    public String getSubAccountRate() {
        return subAccountRate;
    }

    public void setSubAccountRate(String subAccountRate) {
        this.subAccountRate = subAccountRate;
    }

    public String getSubAccountNo() {
        return subAccountNo;
    }

    public void setSubAccountNo(String subAccountNo) {
        this.subAccountNo = subAccountNo;
    }

    public String getSubAccountName() {
        return subAccountName;
    }

    public void setSubAccountName(String subAccountName) {
        this.subAccountName = subAccountName;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsType() {
        return goodsType;
    }

    public void setGoodsType(String goodsType) {
        this.goodsType = goodsType;
    }

    public Integer getGoodsQuantity() {
        return goodsQuantity;
    }

    public void setGoodsQuantity(Integer goodsQuantity) {
        this.goodsQuantity = goodsQuantity;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsPrice() {
        return goodsPrice;
    }

    public void setGoodsPrice(String goodsPrice) {
        this.goodsPrice = goodsPrice;
    }

    public String getVerCode() {
        return verCode;
    }

    public void setVerCode(String verCode) {
        this.verCode = verCode;
    }

    public String getOrderCreateIp() {
        return orderCreateIp;
    }

    public void setOrderCreateIp(String orderCreateIp) {
        this.orderCreateIp = orderCreateIp;
    }

    public PayeeInfo getPayeeInfo() {
        return payeeInfo;
    }

    public void setPayeeInfo(PayeeInfo payeeInfo) {
        this.payeeInfo = payeeInfo;
    }

    public PayerInfo getPayerInfo() {
        return payerInfo;
    }

    public void setPayerInfo(PayerInfo payerInfo) {
        this.payerInfo = payerInfo;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getTimeoutTime() {
        return timeoutTime;
    }

    public void setTimeoutTime(String timeoutTime) {
        this.timeoutTime = timeoutTime;
    }

    public String getMsgCheckCode() {
        return msgCheckCode;
    }

    public void setMsgCheckCode(String msgCheckCode) {
        this.msgCheckCode = msgCheckCode;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    @Override
    public String toString() {
        return "BankPayReqDTO" + GsonUtil.objToJson(this);
    }
}
