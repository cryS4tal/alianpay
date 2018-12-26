package com.ylli.api.pay.service;

import com.google.common.base.Strings;
import com.ylli.api.mch.service.MchKeyService;
import com.ylli.api.pay.model.BankPayOrder;
import com.ylli.api.pay.model.Response;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.service.PingAnService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankPayService {

    @Value("${bank.pay.min}")
    public Integer bankPayMin;

    @Value("${bank.pay.max}")
    public Integer bankPayMax;

    @Autowired
    MchKeyService mchKeyService;

    @Autowired
    PingAnService pingAnService;

    @Transactional
    public Object createOrder(BankPayOrder bankPayOrder) throws Exception {
        //参数校验
        if (bankPayOrder.mchId == null) {
            return new Response("A003", "非法的请求参数：商户号为空", bankPayOrder);
        }
        if (Strings.isNullOrEmpty(bankPayOrder.mchOrderId)) {
            return new Response("A003", "非法的请求参数：商户订单号为空", bankPayOrder);
        }
        //金额校验
        if (bankPayOrder.money == null || bankPayMin > bankPayOrder.money || bankPayMax < bankPayOrder.money) {
            return new Response("A003", "非法的请求参数：金额错误", bankPayOrder);
        }
        if (Strings.isNullOrEmpty(bankPayOrder.accNo)) {
            return new Response("A003", "非法的请求参数：银行卡号为空", bankPayOrder);
        }
        if (Strings.isNullOrEmpty(bankPayOrder.accName)) {
            return new Response("A003", "非法的请求参数：姓名为空", bankPayOrder);
        }
        //代付类型转换 & 校验
        if (bankPayOrder.payType == null) {
            bankPayOrder.payType = BankPayOrder.PAY_TYPE_PERSON;
        }
        if (!BankPayOrder.payAllows.contains(bankPayOrder.payType)) {
            return new Response("A003", "非法的请求参数：代付类型不正确", bankPayOrder);
        }
        if (bankPayOrder.payType == BankPayOrder.PAY_TYPE_PERSON && !BankPayOrder.accAllows.contains(bankPayOrder.accType)) {
            return new Response("A003", "非法的请求参数：账户类型不正确", bankPayOrder);
        }
        //联行号校验
        if (bankPayOrder.payType == BankPayOrder.PAY_TYPE_COMPANY && Strings.isNullOrEmpty(bankPayOrder.issuer)) {
            return new Response("A003", "非法的请求参数：联行号不能为空", bankPayOrder);
        }
        //sign 校验.
        if (Strings.isNullOrEmpty(bankPayOrder.sign)) {
            return new Response("A001", "签名校验失败", bankPayOrder);
        }
        String secretKey = mchKeyService.getKeyById(bankPayOrder.mchId);
        if (secretKey == null) {
            return new Response("A002", "请先上传商户私钥", null);
        }
        if (isSignValid(bankPayOrder, secretKey)) {
            return new Response("A001", "签名校验失败", bankPayOrder);
        }

        //代付通道选择（系统统一切换还是可以按商户单独分配）
        if (true) {
            //TODO 代付订单系统



            //pingAnService.createPingAnOrder();

        }




        return null;
    }

    public boolean isSignValid(BankPayOrder order, String secretKey) throws Exception {
        Map<String, String> map = SignUtil.objectToMap(order);
        return !SignUtil.generateSignature(map, secretKey).equals(order.sign.toUpperCase());
    }
}
