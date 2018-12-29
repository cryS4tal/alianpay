package com.ylli.api.pay.service;

import com.google.common.base.Strings;
import com.ylli.api.mch.service.MchKeyService;
import com.ylli.api.pay.mapper.BankPayOrderMapper;
import com.ylli.api.pay.model.BankPayOrder;
import com.ylli.api.pay.model.OrderQueryReq;
import com.ylli.api.pay.model.OrderQueryRes;
import com.ylli.api.pay.model.Response;
import com.ylli.api.pay.model.SignPayOrder;
import com.ylli.api.pay.util.SerializeUtil;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.service.PingAnService;
import com.ylli.api.third.pay.service.XianFenService;
import com.ylli.api.wallet.model.Wallet;
import com.ylli.api.wallet.service.WalletService;
import java.text.SimpleDateFormat;
import java.util.Map;
import org.modelmapper.ModelMapper;
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

    //手续费暂时设置定值...是否需要修改
    @Value("${bank.pay.charge}")
    public Integer bankPayCharge;

    @Autowired
    BankPayOrderMapper bankPayOrderMapper;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    SerializeUtil serializeUtil;

    @Autowired
    MchKeyService mchKeyService;

    @Autowired
    PingAnService pingAnService;

    @Autowired
    XianFenService xianFenService;

    @Autowired
    WalletService walletService;

    @Transactional
    public Object createOrder(BankPayOrder bankPayOrder) throws Exception {
        //参数校验
        if (bankPayOrder.mchId == null) {
            return new Response("A003", "商户号为空", bankPayOrder);
        }
        if (Strings.isNullOrEmpty(bankPayOrder.mchOrderId)) {
            return new Response("A003", "商户订单号为空", bankPayOrder);
        }
        if (mchOrderExist(bankPayOrder.mchOrderId)) {
            return new Response("A003", "商户订单号重复", bankPayOrder);
        }
        //金额校验
        if (bankPayOrder.money == null || bankPayMin > bankPayOrder.money || bankPayMax < bankPayOrder.money) {
            return new Response("A003", "金额错误", bankPayOrder);
        }
        if (Strings.isNullOrEmpty(bankPayOrder.accNo)) {
            return new Response("A003", "银行卡号为空", bankPayOrder);
        }
        if (Strings.isNullOrEmpty(bankPayOrder.accName)) {
            return new Response("A003", "姓名为空", bankPayOrder);
        }
        //代付类型转换 & 校验
        if (bankPayOrder.payType == null) {
            bankPayOrder.payType = BankPayOrder.PAY_TYPE_PERSON;
        }
        if (!BankPayOrder.payAllows.contains(bankPayOrder.payType)) {
            return new Response("A003", "代付类型不正确", bankPayOrder);
        }
        //默认值
        if (bankPayOrder.payType == BankPayOrder.PAY_TYPE_PERSON &&
                (bankPayOrder.accType != null && !BankPayOrder.accAllows.contains(bankPayOrder.accType))) {
            return new Response("A003", "账户类型不正确", bankPayOrder);
        }
        //联行号校验
        if (bankPayOrder.payType == BankPayOrder.PAY_TYPE_COMPANY && Strings.isNullOrEmpty(bankPayOrder.issuer)) {
            return new Response("A003", "联行号不能为空", bankPayOrder);
        }
        //sign 校验.
        if (Strings.isNullOrEmpty(bankPayOrder.sign)) {
            return new Response("A001", "签名校验失败", bankPayOrder);
        }
        String secretKey = mchKeyService.getKeyById(bankPayOrder.mchId);
        if (secretKey == null) {
            return new Response("A002", "请先上传商户私钥", null);
        }
        if (isSignValid(formatParams(bankPayOrder), secretKey)) {
            return new Response("A001", "签名校验失败", bankPayOrder);
        }
        //TODO 代付手续费暂时设置为定值...是否需要修改
        Wallet wallet = walletService.getOwnWallet(bankPayOrder.mchId);
        if (wallet.reservoir < (bankPayOrder.money + bankPayCharge)) {
            return new Response("A012", "代付余额不足");
        }

        //代付通道选择（系统统一切换还是可以按商户单独分配）
        if (true) {
            //TODO 代付订单系统 , 1 - 平安，2先锋 ，                             1 - 固定
            //平安
            bankPayOrder = insertOrder(bankPayOrder, 1L, BankPayOrder.FIX, 0);

            return pingAnService.createPingAnOrder(bankPayOrder.sysOrderId, bankPayOrder.accNo, bankPayOrder.accName,
                    bankPayOrder.bankName, bankPayOrder.mobile, bankPayOrder.money, secretKey, bankPayOrder.mchOrderId,
                    bankPayOrder.chargeMoney, bankPayOrder.mchId);

        } else {
            //先锋
            bankPayOrder = insertOrder(bankPayOrder, 2L, BankPayOrder.FIX, 0);

            return xianFenService.createXianFenOrder(bankPayOrder.sysOrderId, bankPayOrder.money, bankPayOrder.accNo,
                    bankPayOrder.accName, bankPayOrder.mobile, bankPayOrder.payType, bankPayOrder.accType,
                    bankPayOrder.mchId, secretKey, bankPayOrder.mchOrderId, bankPayOrder.chargeMoney);
        }
    }

    public SignPayOrder formatParams(BankPayOrder bankPayOrder) {
        return modelMapper.map(bankPayOrder, SignPayOrder.class);
    }

    private boolean mchOrderExist(String mchOrderId) {
        BankPayOrder payOrder = new BankPayOrder();
        payOrder.mchOrderId = mchOrderId;
        return bankPayOrderMapper.selectOne(payOrder) != null;
    }

    public boolean isSignValid(SignPayOrder order, String secretKey) throws Exception {
        Map<String, String> map = SignUtil.objectToMap(order);
        return !SignUtil.generateSignature(map, secretKey).equals(order.sign.toUpperCase());
    }

    /**
     * 控制商户代付入参.记录系统代付信息
     */
    public BankPayOrder insertOrder(BankPayOrder bankPayOrder, Long bankPaymentId, Integer chargeType, Integer chargeMoney) {
        bankPayOrder.id = null;
        bankPayOrder.sysOrderId = serializeUtil.generateSysOrderId20();
        bankPayOrder.superOrderId = null;
        bankPayOrder.bankPaymentId = bankPaymentId;
        bankPayOrder.chargeType = chargeType;
        bankPayOrder.chargeMoney = chargeMoney;
        bankPayOrder.isSuccess = null;
        bankPayOrder.status = BankPayOrder.NEW;

        bankPayOrderMapper.insertSelective(bankPayOrder);
        return bankPayOrder;
    }

    public Object orderQuery(OrderQueryReq orderQuery) throws Exception {
        String key = mchKeyService.getKeyById(orderQuery.mchId);
        Map<String, String> map = SignUtil.objectToMap(orderQuery);
        String sign = SignUtil.generateSignature(map, key);
        if (sign.equals(orderQuery.sign.toUpperCase())) {

            BankPayOrder order = bankPayOrderMapper.selectByMchOrderId(orderQuery.mchOrderId);
            if (order == null) {
                return new OrderQueryRes("A006", "订单不存在");
            }
            if (order.status == BankPayOrder.FINISH || order.status == BankPayOrder.FAIL) {

            } else {
                //TODO 主动查询
                /**
                 * 平安代付：
                 * 创建订单 success,扣除代付池金额。加入主动轮询日志，系统自动轮询。
                 * 当轮询返回ok.fail(金额回滚).删除日志，以日志得存在形态来保证不重复入账
                 * 固这里不适合去主动调取平安服务更新状态.
                 */


                //xianfeng query

                //更新订单信息
                //order = bankPayOrderMapper.selectByMchOrderId(orderQuery.mchOrderId);
            }
            //直接返回订单信息
            OrderQueryRes res = new OrderQueryRes();
            res.code = "A000";
            res.message = "成功";
            res.money = order.money;
            res.mchOrderId = order.mchOrderId;
            res.sysOrderId = order.sysOrderId;
            res.status = BankPayOrder.statusToString(order.status);
            if (order.tradeTime != null) {
                res.tradeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(order.tradeTime);
            }

            Map<String, String> map1 = SignUtil.objectToMap(res);
            res.sign = SignUtil.generateSignature(map1, key);
            return res;
        }
        return new OrderQueryRes("A001", "签名校验失败");
    }
}
