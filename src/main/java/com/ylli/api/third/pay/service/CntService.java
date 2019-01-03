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
import com.ylli.api.third.pay.model.CntCard;
import com.ylli.api.third.pay.model.CntCardRes;
import com.ylli.api.third.pay.model.CntCashReq;
import com.ylli.api.third.pay.model.CntRes;
import com.ylli.api.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@EnableAsync
public class CntService {

    @Value("${pay.cnt.secret}")
    public String secret;

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
    @Value("${pay.cont.success_code}")
    public String successCode;
    @Autowired
    SerializeUtil serializeUtil;

    @Transactional
    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, String payType, String tradeType, Object extra) throws Exception {
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);
        String mz = String.format("%.2f", (money / 100.0));
        Integer istype = payType.equals(PayService.ALI) ? CntRes.ZFB_PAY : CntRes.WX_PAY;
        String cntOrder = cntClient.createCntOrder(bill.sysOrderId, mchId.toString() + "_" + bill.id, mz, istype.toString(), CntRes.CNT_BUY.toString());
        CntRes cntRes = new Gson().fromJson(cntOrder, CntRes.class);
        if (successCode.equals(cntRes.resultCode)) {
            CntCard cntCard = cntRes.data.pays.stream().filter(item -> item.payType == istype).findFirst().get();
            bill.superOrderId = cntRes.data.orderId;
            bill.reserve = cntCard.cardId.toString();
            billMapper.updateByPrimaryKeySelective(bill);
            return cntCard.payUrl;
        }
        return null;
    }

    public Response payConfirm(String mchOrderId, Long mchId) throws Exception {
        Bill bill = new Bill();
        bill.mchOrderId = mchOrderId;
        bill = billMapper.selectOne(bill);
        if (bill == null || Strings.isNullOrEmpty(bill.superOrderId)) {
            return new Response("A006", "订单不存在");
        }
        String confirm = cntClient.confirm(bill.superOrderId, bill.reserve);
        CntRes cntRes = new Gson().fromJson(confirm, CntRes.class);
        if (successCode.equals(cntRes.resultCode)) {
            return new Response("A000", "确认成功");
        }
        return new Response("A099", cntRes.resultMsg);
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
        StringBuffer sb = new StringBuffer();
        sb.append(userId).append("|").append(orderId).append("|").append(userOrder).append("|").append(number).append("|").append(isPur).append("|")
                .append(merPriv).append("|").append(remark).append("|").append(date).append("|")
                .append(resultCode).append("|").append(resultMsg).append("|").append(appID).append("|").append(secret);
        String sign = SignUtil.MD5(sb.toString()).toLowerCase();
        System.out.println(sign);
        if (successCode.equals(resultCode) && chkValue.equals(sign)) {
            if (CntRes.CNT_BUY == Integer.parseInt(isPur)) {
                Bill bill = new Bill();
                bill.sysOrderId = userOrder;
                bill = billMapper.selectOne(bill);
                if (bill == null) {
                    return "order not found";
                }
                if (bill.status != Bill.FINISH) {
                    bill.status = Bill.FINISH;
                    bill.tradeTime = Timestamp.from(Instant.now());
                    bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
                    bill.superOrderId = userOrder;
                    bill.msg = resultMsg;
                    billMapper.updateByPrimaryKeySelective(bill);

                    //钱包金额变动。
                    walletService.incr(bill.mchId, bill.money - bill.payCharge);
                }

                //加入异步通知下游商户系统
                //params jsonStr.
                String params = payService.generateRes(
                        bill.money.toString(),
                        bill.mchOrderId,
                        bill.sysOrderId,
                        bill.status == Bill.FINISH ? "S" : bill.status == Bill.FAIL ? "F" : "I",
                        bill.tradeTime == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime),
                        bill.reserve);

                payClient.sendNotify(bill.id, bill.notifyUrl, params, true);

                return "success";
            } else {
                return "";
            }
        } else {
            return "fail";
        }
    }

    @Transactional
    public Object payCancel(String orderId, Long mchId, String sgin) throws Exception {
        Bill bill = new Bill();
        bill.sysOrderId = orderId;
        bill = billMapper.selectOne(bill);
        if (bill == null || Strings.isNullOrEmpty(bill.superOrderId)) {
            return "order not found";
        }
        String confirm = cntClient.cancel(bill.superOrderId, mchId.toString());
        CntRes cntRes = new Gson().fromJson(confirm, CntRes.class);
        if (successCode.equals(cntRes.resultCode)) {
            bill.status = Bill.FAIL;
            billMapper.updateByPrimaryKeySelective(bill);
            return "success";
        }
        return "fail";
    }


    public Object addCard(Long mchId, String userName, String payName, String openBank, String subbranch) throws Exception {
        cntClient.addCard(mchId.toString(), userName, payName, openBank, subbranch);
        return "fail";
    }

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
        Bill bill = billService.createBill(req.mchId, null, 5L, CntRes.ZFB_PAY.toString(), "", req.money, cardId, "", "");
        String cntOrder = cntClient.createCntOrder(bill.sysOrderId, req.mchId.toString(), mz, CntRes.ZFB_PAY.toString(), CntRes.CNT_CASH.toString());
        CntRes cntOrderRes = new Gson().fromJson(cntOrder, CntRes.class);
        if (!successCode.equals(cntOrderRes.resultCode)) {
            return new Response("A099", cntCardRes.resultMsg);
        }
        return new Response("A000", cntCardRes.resultMsg);
    }
}
