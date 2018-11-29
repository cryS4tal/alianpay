package com.ylli.api.yfbpay.service;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.pay.model.Response;
import com.ylli.api.pay.util.SerializeUtil;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.user.model.UserKey;
import com.ylli.api.user.service.UserKeyService;
import com.ylli.api.yfbpay.Config;
import com.ylli.api.yfbpay.mapper.YfbBillMapper;
import com.ylli.api.yfbpay.model.NotifyRes;
import com.ylli.api.yfbpay.model.YfbBill;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@EnableAsync
public class YfbService {

    private static Logger LOGGER = LoggerFactory.getLogger(YfbService.class);

    @Autowired
    YfbClient yfbClient;

    @Autowired
    SerializeUtil serializeUtil;

    @Autowired
    YfbBillMapper yfbBillMapper;

    @Autowired
    UserKeyService userKeyService;

    @Transactional(rollbackFor = AwesomeException.class)
    public Object createOrder(Long mchId, String payType, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, Object extra) throws Exception {
        //加入商户系统订单.
        //todo 综合账单加入支付类型
        YfbBill bill = new YfbBill();
        bill.amount = money;
        bill.status = YfbBill.NEW;
        bill.userId = mchId;
        bill.memo = reserve;
        bill.notifyUrl = notifyUrl;
        bill.redirectUrl = redirectUrl;
        bill.subNo = mchOrderId;
        yfbBillMapper.insertSelective(bill);
        bill = yfbBillMapper.selectOne(bill);
        String orderNo = serializeUtil.generateOrderNo(SerializeUtil.YFB_PAY, mchId, bill.id);
        bill.orderNo = orderNo;
        yfbBillMapper.updateByPrimaryKeySelective(bill);

        String str = yfbClient.order(payType, String.valueOf(((double) money / 100)), orderNo, notifyUrl, redirectUrl, null, reserve);
        if (str.contains("error")) {
            throw new AwesomeException(Config.ERROR_SERVER_CONNECTION);
        }
        return str;
    }

    public boolean exist(String mchOrderId) {
        YfbBill bill = new YfbBill();
        bill.subNo = mchOrderId;
        return yfbBillMapper.selectOne(bill) != null;
    }

    @Transactional
    public String payNotify(String orderid, String opstate, String ovalue, String sign, String sysorderid, String systime, String attach, String msg) throws Exception {
        if (Strings.isNullOrEmpty(orderid) || Strings.isNullOrEmpty(opstate) || Strings.isNullOrEmpty(ovalue)
                || Strings.isNullOrEmpty(sign) || Strings.isNullOrEmpty(sysorderid) || Strings.isNullOrEmpty(systime)) {
            return "opstate=-1";
        }
        boolean flag = yfbClient.signVerify(orderid, opstate, ovalue, sign);
        if (flag) {
            YfbBill bill = new YfbBill();
            bill.orderNo = orderid;
            bill = yfbBillMapper.selectOne(bill);
            if (bill == null) {
                return "opstate=-1";
            }

            if (opstate.equals("0") || opstate.equals("-3")) {
                bill.superNo = sysorderid;
                bill.tradeTime = new Timestamp(new SimpleDateFormat("YYYY/MM/DD hh:mm:ss").parse(systime).getTime());
                bill.status = YfbBill.FINISH;
                bill.msg = msg;
                yfbBillMapper.updateByPrimaryKeySelective(bill);

            } else {
                bill.superNo = sysorderid;
                bill.tradeTime = new Timestamp(new SimpleDateFormat("YYYY/MM/DD hh:mm:ss").parse(systime).getTime());
                bill.status = YfbBill.FAIL;
                bill.msg = msg;
                yfbBillMapper.updateByPrimaryKeySelective(bill);

            }

            //加入异步通知下游商户系统
            //params jsonStr.
            yfbClient.sendNotify(bill.id, bill.notifyUrl, generateRes(
                    bill.amount.toString(),
                    bill.subNo,
                    bill.orderNo,
                    bill.status == YfbBill.FINISH ? "S" : bill.status == YfbBill.FAIL ? "F" : "I",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime),
                    bill.memo));

            if (bill.isSuccess != null && bill.isSuccess) {
                return "opstate=0";
            } else {
                //隐藏真实返回.待商户系统返回success再做正确处理
                return "";
            }
        } else {
            return "opstate=-2";
        }

    }

    /**
     * 金额
     * 子系统订单干号
     * 我们系统订单号
     * 状态
     * 交易时间
     * sign
     * 保留域
     */
    public String generateRes(String money, String mchOrderId, String sysOrderId, String status, String tradeTime, String reserve) throws Exception {
        NotifyRes res = new NotifyRes();
        res.money = money;
        res.mchOrderId = mchOrderId;
        res.sysOrderId = sysOrderId;
        res.status = status;
        res.tradeTime = tradeTime;
        res.reserve = reserve;

        YfbBill bill = new YfbBill();
        bill.orderNo = sysOrderId;
        bill = yfbBillMapper.selectOne(bill);
        UserKey key = userKeyService.getKeyByUserId(bill.userId);

        Map<String, String> map = SignUtil.objectToMap(res);
        res.sign = SignUtil.generateSignature(map, key.secretKey);
        return new Gson().toJson(res);
    }


    public YfbBill selectByMchOrderId(String mchOrderId) {
        YfbBill bill = new YfbBill();
        bill.subNo = mchOrderId;
        return yfbBillMapper.selectOne(bill);
    }

    public YfbBill orderQuery(String mchOrderId) throws Exception {
        String str =  yfbClient.orderQuery(mchOrderId);

        /**
         * todo .
         * 对易付宝返回结果进行逻辑处理
         */
        YfbBill bill = new YfbBill();
        bill.subNo = mchOrderId;
        return yfbBillMapper.selectOne(bill);
    }
}
