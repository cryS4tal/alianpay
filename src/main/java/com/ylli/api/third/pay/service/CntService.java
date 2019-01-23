package com.ylli.api.third.pay.service;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.mch.service.MchKeyService;
import com.ylli.api.mch.service.RateService;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import com.ylli.api.pay.service.PayClient;
import com.ylli.api.pay.service.PayService;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.enums.CNTEnum;
import com.ylli.api.third.pay.model.CNTCard;
import com.ylli.api.third.pay.model.CntRes;
import com.ylli.api.wallet.mapper.CashLogMapper;
import com.ylli.api.wallet.model.CashLog;
import com.ylli.api.wallet.service.WalletService;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@EnableAsync
public class CntService {

    @Value("${pay.cnt.secret}")
    public String secret;

    @Value("${pay.cnt.appid}")
    public String appId;

    @Value("${pay.cnt.apihost}")
    public String apihost;

    @Autowired
    BillService billService;

    @Autowired
    CntClient cntClient;

    @Autowired
    PayClient payClient;

    @Autowired
    PayService payService;

    @Autowired
    BillMapper billMapper;

    @Autowired
    CashLogMapper cashLogMapper;

    @Autowired
    WalletService walletService;

    @Autowired
    RateService appService;

    @Autowired
    MchKeyService mchKeyService;

    public static String successCode = "0000";

    /**
     * cnt下单
     *
     * @param mchId       商户订单号
     * @param channelId   通道id
     * @param money       金额
     * @param mchOrderId  系统订单号
     * @param notifyUrl
     * @param redirectUrl
     * @param reserve
     * @param payType     支付类型
     * @param tradeType   支付方式
     * @param extra
     * @return
     * @throws Exception
     */
    @Transactional
    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, String payType, String tradeType, Object extra) throws Exception {
        //创建订单
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);

        //分转换元
        String mz = String.format("%.2f", (money / 100.0));
        //支付类型转为上游类型
        String istype = payType.equals(PayService.ALI) ? CNTEnum.ALIPAY.getValue() : CNTEnum.WX.getValue();

        //向上游发起下單请求
        String cntOrder = cntClient.createCntOrder(bill.sysOrderId, mchId.toString() + "_" + bill.id, mz, istype, CNTEnum.BUY.getValue());
        CntRes cntRes = new Gson().fromJson(cntOrder, CntRes.class);
        if (successCode.equals(cntRes.resultCode)) {
            //记录上游的订单号和卡号
            CNTCard cntCard = cntRes.data.pays.stream().filter(item -> String.valueOf(item.payType).equals(istype)).findFirst().get();
            bill.superOrderId = cntRes.data.orderId;
            bill.reserve = cntCard.cardId.toString();
            bill.reserveWord = cntRes.data.referenceCode;
            billMapper.updateByPrimaryKeySelective(bill);
            //返回支付链接

            //支付链接封装成我们自己的路由url.
            String returnUrl = UriComponentsBuilder.fromHttpUrl(
                    apihost)
                    .queryParam("money", bill.money)
                    .queryParam("order_id", bill.superOrderId)
                    .queryParam("link", cntCard.payUrl)
                    .queryParam("mch_id", bill.mchId)
                    .queryParam("mch_order_id", bill.mchOrderId)
                    .build().toUriString();
            return returnUrl;
        } else {
            //下单失败
            bill.status = Bill.FAIL;
            billMapper.updateByPrimaryKeySelective(bill);
            return null;
        }
    }

    /**
     * @param userId     商户号
     * @param orderId    订单号
     * @param userOrder  用户系统订单号
     * @param number     数量
     * @param date       时间
     * @param resultCode 状态码 0000 成功 其他为失败
     * @param resultMsg  状态描述
     * @param appID      APPID
     * @return
     */
    @Transactional
    public String payNotify(String userId, String orderId, String userOrder, String number, String remark, String merPriv, String date, String resultCode, String resultMsg, String appID, String isPur, String chkValue) throws Exception {

        String sign = generateSign(userId, orderId, userOrder, number, remark, merPriv, date, resultCode, resultMsg, appID);

        //签名验证
        if (chkValue.equals(sign)) {
            //支付回调
            if (CNTEnum.BUY.getValue().equals(isPur)) {
                Bill bill = billService.selectBySysOrderId(userOrder);
                if (bill == null) {
                    return "order not found";
                }
                //fix cnt 异步回调多次返回状态不一致.... 暂时以第一次为准。
                if (bill.status == Bill.FINISH || bill.status == Bill.FAIL) {
                    return "repeat notify";
                }

                if (successCode.equals(resultCode)) {
                    //交易成功
                    if (bill.status != Bill.FINISH) {
                        bill.status = Bill.FINISH;
                        bill.tradeTime = string2Timestamp(date);
                        bill.superOrderId = orderId;

                        //以实际成交金额计算。
                        bill.msg = number;
                        bill.payCharge = ((Double.valueOf(Optional.ofNullable(number).orElse("0")).intValue() * 100) * appService.getRate(bill.mchId, bill.appId)) / 10000;
                        billMapper.updateByPrimaryKeySelective(bill);

                        //钱包金额变动。
                        walletService.incr(bill.mchId, bill.money, bill.payCharge, bill.payType);
                    }

                } else {
                    //交易失败
                    if (bill.status != Bill.FAIL) {
                        bill.status = Bill.FAIL;
                        bill.tradeTime = string2Timestamp(date);
                        bill.superOrderId = orderId;
                        //bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
                        bill.msg = number;
                        billMapper.updateByPrimaryKeySelective(bill);
                    }
                }

                //加入异步通知下游商户系统
                //params jsonStr.
                if (!Strings.isNullOrEmpty(bill.notifyUrl)) {
                    String params = payService.generateRes(
                            //fix 用户手动输入金额会导致  实际交易金额和下单金额不一致
                            bill.money.toString(),
                            bill.mchOrderId,
                            bill.sysOrderId,
                            bill.status == Bill.FINISH ? "S" : bill.status == Bill.FAIL ? "F" : "I",
                            bill.tradeTime == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime),
                            bill.reserve);

                    payClient.sendNotify(bill.id, bill.notifyUrl, params, true);
                }
                return "success";
            } else if (CNTEnum.CASH.getValue().equals(isPur)) {
                //提现回调
                //TODO
                CashLog log = cashLogMapper.selectByPrimaryKey(Long.parseLong(userOrder));
                if (log == null) {
                    return "cash log 404 not found";
                }
                if (log.state == CashLog.PROCESS) {

                    if (successCode.equals(resultCode)) {
                        //提现成功
                        log.state = CashLog.FINISH;
                        cashLogMapper.updateByPrimaryKeySelective(log);

                        walletService.cashSuc(log.mchId, log.money);
                    } else {
                        //提现失败
                        log.state = CashLog.FAILED;
                        cashLogMapper.updateByPrimaryKeySelective(log);

                        walletService.cashFail(log.mchId, log.money);
                    }
                }
                return "success";
            } else {
                //返回类型错误
                return "isPur error.";
            }
        } else {
            //签名校验失败
            return "sign error.";
        }
    }

    public Timestamp string2Timestamp(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return new Timestamp(sdf.parse(date).getTime());
    }

    public String generateSign(String userId, String orderId, String userOrder, String number, String remark,
                               String merPriv, String date, String resultCode, String resultMsg, String appID) throws Exception {
        String str = new StringBuffer()
                .append(userId).append("|")
                .append(orderId).append("|")
                .append(userOrder).append("|")
                .append(number).append("|")
                .append(merPriv).append("|")
                .append(remark).append("|")
                .append(date).append("|")
                .append(resultCode).append("|")
                .append(resultMsg).append("|")
                .append(appID).append("|").append(secret).toString();
        String sign = SignUtil.MD5(str).toLowerCase();

        return sign;
    }

    public String confirm(String superOrderId, String reserve) throws Exception {
        return cntClient.confirm(superOrderId, reserve);
    }
}
