package com.ylli.api.yfbpay.service;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.pay.model.SumAndCount;
import static com.ylli.api.pay.service.PayService.ALI;
import static com.ylli.api.pay.service.PayService.NATIVE;
import static com.ylli.api.pay.service.PayService.WAP;
import static com.ylli.api.pay.service.PayService.WX;
import com.ylli.api.pay.util.SerializeUtil;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.user.mapper.UserSettlementMapper;
import com.ylli.api.user.model.UserKey;
import com.ylli.api.user.model.UserSettlement;
import com.ylli.api.user.service.UserKeyService;
import com.ylli.api.wallet.service.WalletService;
import com.ylli.api.yfbpay.Config;
import com.ylli.api.yfbpay.mapper.YfbBillMapper;
import com.ylli.api.yfbpay.model.NotifyRes;
import com.ylli.api.yfbpay.model.YfbBill;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
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

    public static final String YFB_ALI_NATIVE = "992";
    public static final String YFB_WX_NATIVE = "1004";

    public static final String YFB_ALI_WAP = "2098";
    public static final String YFB_WX_WAP = "2099";

    @Autowired
    YfbClient yfbClient;

    @Autowired
    SerializeUtil serializeUtil;

    @Autowired
    YfbBillMapper yfbBillMapper;

    @Autowired
    UserKeyService userKeyService;

    @Autowired
    UserSettlementMapper userSettlementMapper;

    @Autowired
    WalletService walletService;

    @Transactional(rollbackFor = AwesomeException.class)
    public String createOrder(Long mchId, String payType, String tradeType, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, Object extra) throws Exception {
        //加入商户系统订单.
        if (tradeType == null) {
            //支付方式. 默认扫码
            tradeType = NATIVE;
        }
        YfbBill bill = new YfbBill();
        bill.amount = money;
        bill.status = YfbBill.NEW;
        bill.userId = mchId;
        bill.memo = reserve;
        bill.notifyUrl = notifyUrl;
        bill.redirectUrl = redirectUrl;
        bill.subNo = mchOrderId;
        bill.payType = payType;
        bill.tradeType = tradeType;
        yfbBillMapper.insertSelective(bill);
        bill = yfbBillMapper.selectOne(bill);
        String orderNo = serializeUtil.generateOrderNo(SerializeUtil.YFB_PAY, mchId, bill.id);
        bill.orderNo = orderNo;
        yfbBillMapper.updateByPrimaryKeySelective(bill);

        String str = yfbClient.order(typeConvert(null, payType, tradeType), String.valueOf(((double) money / 100)), orderNo, notifyUrl, redirectUrl, null, reserve);
        if (str.contains("error")) {
            throw new AwesomeException(Config.ERROR_SERVER_CONNECTION);
        }
        return str;
    }

    /**
     * 支付类型转换
     *
     * @return
     */
    public String typeConvert(Long channelId, String payType, String tradeType) {
        if (true) { //channelId 为通道id.
            if (payType.equals(ALI) && tradeType.equals(NATIVE)) {

                return YFB_ALI_NATIVE;
            } else if (payType.equals(ALI) && tradeType.equals(WAP)) {

                return YFB_ALI_WAP;
            } else if (payType.equals(WX) && tradeType.equals(NATIVE)) {

                return YFB_WX_NATIVE;
            } else if (payType.equals(WX) && tradeType.equals(WAP)) {

                return YFB_WX_WAP;
            }
        }
        return "";
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
                //todo  这部分后续记得修改。。。   settlement 信息在用户激活时插入。
                UserSettlement settlement = userSettlementMapper.selectByUserId(bill.userId);
                if (settlement != null && bill.status != YfbBill.FINISH) {
                    walletService.addBonus(bill.userId, bill.id, settlement.chargeRate);
                }
                bill.superNo = sysorderid;
                bill.tradeTime = new Timestamp(new SimpleDateFormat("YYYY/MM/DD hh:mm:ss").parse(systime).getTime());
                bill.status = YfbBill.FINISH;
                //todo msg暂时先记录实际交易金额/元 两位小数
                bill.msg = ovalue;
                //bill.msg = msg;

                yfbBillMapper.updateByPrimaryKeySelective(bill);

            } else {
                bill.superNo = sysorderid;
                bill.tradeTime = new Timestamp(new SimpleDateFormat("YYYY/MM/DD hh:mm:ss").parse(systime).getTime());
                bill.status = YfbBill.FAIL;
                //todo msg暂时先记录实际交易金额/元 两位小数
                bill.msg = ovalue;
                //bill.msg = msg;
                yfbBillMapper.updateByPrimaryKeySelective(bill);
            }

            //加入异步通知下游商户系统
            //params jsonStr.
            String params = generateRes(
                    bill.amount.toString(),
                    bill.subNo,
                    bill.orderNo,
                    bill.status == YfbBill.FINISH ? "S" : bill.status == YfbBill.FAIL ? "F" : "I",
                    bill.tradeTime == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime),
                    bill.memo);

            yfbClient.sendNotify(bill.id, bill.notifyUrl, params);

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
        String str = yfbClient.orderQuery(mchOrderId);
        //orderid=20181203040702B000100200000025&opstate=0&ovalue=100.00&sign=52aeb9d6083a43130f3050468a37e30c&msg=查询成功
        str = StringUtils.substringAfter(str, "=");
        String orderid = StringUtils.substringBefore(str, "&");

        str = StringUtils.substringAfter(str, "=");
        String opstate = StringUtils.substringBefore(str, "&");

        str = StringUtils.substringAfter(str, "=");
        String ovalue = StringUtils.substringBefore(str, "&");

        str = StringUtils.substringAfter(str, "=");
        String sign = StringUtils.substringBefore(str, "&");

        boolean flag = yfbClient.signVerify(orderid, opstate, ovalue, sign);
        YfbBill bill = new YfbBill();
        bill.orderNo = mchOrderId;
        bill = yfbBillMapper.selectOne(bill);
        if (flag && (bill.status == YfbBill.NEW || bill.status == YfbBill.ING)) {
            if (opstate.equals("0")) {
                bill.status = YfbBill.FINISH;
                bill.msg = ovalue;

                UserSettlement settlement = userSettlementMapper.selectByUserId(bill.userId);
                if (settlement != null) {
                    walletService.addBonus(bill.userId, bill.id, settlement.chargeRate);
                }
                yfbBillMapper.updateByPrimaryKeySelective(bill);
            }
            //其他情况 暂时不做处理.
        }
        return bill;
    }

    public List<YfbBill> getBills(Long userId,
                                  Integer status,
                                  String mchOrderId,
                                  String sysOrderId,
                                  String payType,
                                  String tradeType,
                                  Date tradeTime,
                                  Date startTime,
                                  Date endTime) {
        return yfbBillMapper.getBills(userId, status, mchOrderId, sysOrderId, payType, tradeType, tradeTime, startTime, endTime);
    }

    public SumAndCount getTodayDetail(Long userId) {
        SumAndCount sumAndCount = yfbBillMapper.getTodayDetail(userId);
        if (sumAndCount.total == null) {
            sumAndCount.total = 0L;
        }
        return sumAndCount;
    }

    public Integer getMaxCash(Long userId) {
        Integer max = yfbBillMapper.getMaxCash(userId);
        return max;
    }

    @Transactional
    public void closeExpiredBill() {
        yfbBillMapper.closeExpiredBill();
    }

    public String notifyTest(String mchOrderId) throws Exception {
        YfbBill bill = new YfbBill();
        bill.subNo = mchOrderId;
        bill = yfbBillMapper.selectOne(bill);
        if (bill == null) {
            return "订单不存在";
        }
        String params = generateRes(
                bill.amount.toString(),
                bill.subNo,
                bill.orderNo,
                bill.status == YfbBill.FINISH ? "S" : bill.status == YfbBill.FAIL ? "F" : "I",
                bill.tradeTime == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime),
                bill.memo);

        //yfbClient.sendNotify(bill.id, bill.notifyUrl, params);
        return params;
    }
}
