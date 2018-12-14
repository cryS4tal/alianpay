package com.ylli.api.third.pay.service;

import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.PayClient;
import com.ylli.api.pay.service.PayService;
import com.ylli.api.pay.util.SerializeUtil;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.user.service.AppService;
import com.ylli.api.wallet.service.WalletService;
import com.ylli.api.third.pay.model.YfbBill;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@EnableAsync
public class WzService {

    @Autowired
    WzClient wzClient;

    @Autowired
    PayClient payClient;

    @Autowired
    SerializeUtil serializeUtil;

    @Autowired
    BillMapper billMapper;

    @Autowired
    AppService appService;

    @Autowired
    WalletService walletService;

    @Autowired
    PayService payService;

    @Value("${pay.wz.id}")
    public String mchid;

    @Value("${pay.wz.secret}")
    public String secret;

    @Transactional
    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, String payType, String tradeType, Object extra) throws Exception {
        Bill bill = new Bill();
        bill.mchId = mchId;
        bill.sysOrderId = serializeUtil.generateSysOrderId();
        bill.mchOrderId = mchOrderId;
        bill.channelId = channelId;
        // todo 应用模块 关联.
        bill.appId = appService.getAppId(payType, tradeType);

        bill.money = money;
        bill.status = Bill.NEW;
        bill.reserve = reserve;
        bill.notifyUrl = notifyUrl;
        bill.redirectUrl = redirectUrl;
        bill.payType = payType;
        bill.tradeType = tradeType;
        billMapper.insertSelective(bill);

        String mz = String.format("%.2f", (money / 100.0));
        //商品名称 = 商户号
        return wzClient.createWzOrder(bill.sysOrderId, mz, reserve, mchId.toString(), redirectUrl, mchId.toString());
    }


    @Transactional
    public String payNotify(Long spid, String md5, String oid, String sporder, String mz, String zdy, Long spuid) throws Exception {

        StringBuffer sb = new StringBuffer().append(oid).append(sporder).append(mchid).append(mz).append(secret);
        String sign = SignUtil.MD5(sb.toString());
        if (sign.equals(md5)) {
            Bill bill = new Bill();
            bill.sysOrderId = sporder;
            bill = billMapper.selectOne(bill);
            if (bill == null) {
                return "order not found";
            }
            if (bill.status != Bill.FINISH){
                bill.status = Bill.FINISH;
                bill.tradeTime = Timestamp.from(Instant.now());
                bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
                bill.superOrderId = oid;
                bill.msg = mz;
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
                    bill.status == YfbBill.FINISH ? "S" : bill.status == YfbBill.FAIL ? "F" : "I",
                    bill.tradeTime == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime),
                    bill.reserve);

            payClient.sendNotify(bill.id, bill.notifyUrl, params);

            if (bill.isSuccess != null && bill.isSuccess) {
                return "ok";
            } else {
                //隐藏真实返回.待商户系统返回success再做正确处理
                return "";
            }
        } else {
            return "fail";
        }

    }
}