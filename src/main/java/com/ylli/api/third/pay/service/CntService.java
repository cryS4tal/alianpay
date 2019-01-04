package com.ylli.api.third.pay.service;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.mch.service.AppService;
import com.ylli.api.mch.service.MchKeyService;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.model.Response;
import com.ylli.api.pay.service.BillService;
import com.ylli.api.pay.service.PayClient;
import com.ylli.api.pay.service.PayService;
import com.ylli.api.pay.util.SerializeUtil;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.enums.CNTEnum;
import com.ylli.api.third.pay.model.CntCard;
import com.ylli.api.third.pay.model.CntCardRes;
import com.ylli.api.third.pay.model.CntCashReq;
import com.ylli.api.third.pay.model.CntRes;
import com.ylli.api.wallet.service.WalletService;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@EnableAsync
public class CntService {

    @Value("${pay.cnt.secret}")
    public String secret;

    @Value("${pay.cnt.appid}")
    public String appId;

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
    WalletService walletService;

    @Autowired
    AppService appService;

    @Autowired
    MchKeyService mchKeyService;

    public static String successCode = "0000";

    @Autowired
    SerializeUtil serializeUtil;

    /**
     * 创建cnt订单
     */
    @Transactional
    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, String payType, String tradeType, Object extra) throws Exception {
        //创建订单
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);

        //分转换元
        String mz = String.format("%.2f", (money / 100.0));
        //上游支付宝or微信的标识
        String istype = payType.equals(PayService.ALI) ? CNTEnum.ALIPAY.getValue() : CNTEnum.WX.getValue();

        //向上游发起下定请求
        String cntOrder = cntClient.createCntOrder(bill.sysOrderId, mchId.toString() + "_" + bill.id, mz, istype, CNTEnum.BUY.getValue());
        CntRes cntRes = new Gson().fromJson(cntOrder, CntRes.class);
        if (successCode.equals(cntRes.resultCode)) {
            //记录上游的定单号和卡号
            CntCard cntCard = cntRes.data.pays.stream().filter(item -> String.valueOf(item.payType).equals(istype)).findFirst().get();
            bill.superOrderId = cntRes.data.orderId;
            bill.reserve = cntCard.cardId.toString();
            billMapper.updateByPrimaryKeySelective(bill);
            //返回支付链接
            return cntCard.payUrl;
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

        //TODO PARAMS check.
        if (chkValue.equals(sign)) {
            //支付回调
            if (CNTEnum.BUY.getValue().equals(isPur)) {
                Bill bill = billService.selectBySysOrderId(userOrder);
                if (bill == null) {
                    return "order not found";
                }
                bill.money = (int) Double.parseDouble(number) * 100;
                if (successCode.equals(resultCode)) {
                    //交易成功
                    if (bill.status != Bill.FINISH) {
                        bill.status = Bill.FINISH;
                        bill.superOrderId = orderId;
                        bill.tradeTime = convertTs(date);

                        bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
                        bill.superOrderId = userOrder;
                        bill.msg = number;
                        billMapper.updateByPrimaryKeySelective(bill);

                        //钱包金额变动。
                        walletService.incr(bill.mchId, bill.money - bill.payCharge);
                    }

                } else {
                    //交易失败
                    if (bill.status != Bill.FAIL) {
                        bill.status = Bill.FAIL;
                        bill.superOrderId = orderId;
                        bill.tradeTime = convertTs(date);
                        bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
                        bill.superOrderId = userOrder;
                        bill.msg = number;
                        billMapper.updateByPrimaryKeySelective(bill);
                    }
                }

                //加入异步通知下游商户系统
                //params jsonStr.
                if (!Strings.isNullOrEmpty(bill.notifyUrl)) {
                    String params = payService.generateRes(
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


                return "";
            } else {
                //返回类型错误
                return "isPur error.";
            }
        } else {
            //签名校验失败
            return "sign error.";
        }
    }

    public Timestamp convertTs(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sdf.parse(date));
        return new Timestamp(calendar.getTime().getTime());
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


    /**
     * 提现
     *
     * @param req
     * @return
     * @throws Exception
     */
    public Object cash(CntCashReq req) throws Exception {
        //查询卡列表
        CntCardRes cntCardRes = new Gson().fromJson(cntClient.findCards(req.mchId.toString()), CntCardRes.class);
        if (!successCode.equals(cntCardRes.resultCode))
            return new Response("A099", cntCardRes.resultMsg);
        List<CntCard> data = cntCardRes.data;
        if (null != data && data.size() > 0) {
            for (CntCard card : data) {
                //删除卡
                CntCardRes delRes = new Gson().fromJson(cntClient.delCard(card.id.toString()), CntCardRes.class);
                if (!successCode.equals(delRes.resultCode))
                    return new Response("A099", delRes.resultCode);
            }
        }
        //添加卡
        CntRes cntRes = new Gson().fromJson(cntClient.addCard(req.mchId.toString(), req.userName, req.payName, req.openBank, req.subbranch), CntRes.class);
        if (!successCode.equals(cntRes.resultCode))
            return new Response("A099", cntCardRes.resultMsg);
        String cardId = cntRes.data.cardId;
        //下单
        String mz = String.format("%.2f", (req.money / 100.0));
        Bill bill = billService.createBill(req.mchId, null, 5L, CNTEnum.ALIPAY.getValue(), "", req.money, cardId, "", "");
        String cntOrder = cntClient.createCntOrder(bill.sysOrderId, req.mchId.toString(), mz, CNTEnum.ALIPAY.getValue(), CNTEnum.CASH.getValue());
        CntRes cntOrderRes = new Gson().fromJson(cntOrder, CntRes.class);
        if (!successCode.equals(cntOrderRes.resultCode)) {
            return new Response("A099", cntCardRes.resultMsg);
        }
        return new Response("A000", cntCardRes.resultMsg);
    }

    public String confirm(String superOrderId, String reserve) throws Exception {
        return cntClient.confirm(superOrderId, reserve);
    }
}
