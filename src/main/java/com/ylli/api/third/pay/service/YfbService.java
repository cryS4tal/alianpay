package com.ylli.api.third.pay.service;

import com.google.common.base.Strings;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.PayClient;
import com.ylli.api.pay.service.PayService;
import static com.ylli.api.pay.service.PayService.ALI;
import static com.ylli.api.pay.service.PayService.NATIVE;
import static com.ylli.api.pay.service.PayService.WAP;
import static com.ylli.api.pay.service.PayService.WX;
import com.ylli.api.pay.util.SerializeUtil;
import com.ylli.api.third.pay.Config;
import com.ylli.api.third.pay.model.YfbBill;
import com.ylli.api.user.service.AppService;
import com.ylli.api.wallet.service.WalletService;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@EnableAsync
public class YfbService {

    public static final String YFB_ALI_NATIVE = "992";
    public static final String YFB_WX_NATIVE = "1004";

    public static final String YFB_ALI_WAP = "2098";
    public static final String YFB_WX_WAP = "2099";

    @Autowired
    YfbClient yfbClient;

    @Autowired
    SerializeUtil serializeUtil;

    @Autowired
    WalletService walletService;

    @Autowired
    AppService appService;

    @Autowired
    PayService payService;

    @Autowired
    PayClient payClient;

    @Autowired
    BillMapper billMapper;

    @Transactional(rollbackFor = AwesomeException.class)
    public String createOrder(Long mchId, Long channelId, String payType, String tradeType, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, Object extra) throws Exception {
        //加入商户系统订单.
        if (tradeType == null) {
            //支付方式. 默认扫码
            tradeType = NATIVE;
        }
        Bill bill = new Bill();
        bill.money = money;
        bill.status = Bill.NEW;
        bill.mchId = mchId;
        bill.reserve = reserve;
        bill.notifyUrl = notifyUrl;
        bill.redirectUrl = redirectUrl;
        bill.mchOrderId = mchOrderId;
        bill.payType = payType;
        bill.tradeType = tradeType;
        bill.sysOrderId = serializeUtil.generateSysOrderId();

        bill.channelId = channelId;
        // todo 应用模块 关联.
        bill.appId = appService.getAppId(payType, tradeType);

        billMapper.insertSelective(bill);

        String str = yfbClient.order(typeConvert(null, payType, tradeType), String.valueOf(((double) money / 100)), bill.sysOrderId, notifyUrl, redirectUrl, null, reserve);
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


    @Transactional
    public String payNotify(String orderid, String opstate, String ovalue, String sign, String sysorderid, String systime, String attach, String msg) throws Exception {
        if (Strings.isNullOrEmpty(orderid) || Strings.isNullOrEmpty(opstate) || Strings.isNullOrEmpty(ovalue)
                || Strings.isNullOrEmpty(sign) || Strings.isNullOrEmpty(sysorderid) || Strings.isNullOrEmpty(systime)) {
            return "opstate=-1";
        }
        boolean flag = yfbClient.signVerify(orderid, opstate, ovalue, sign);
        if (flag) {
            Bill bill = new Bill();
            //bill.orderNo = orderid;
            bill.sysOrderId = orderid;
            bill = billMapper.selectOne(bill);
            if (bill == null) {
                return "opstate=-1";
            }

            if (opstate.equals("0") || opstate.equals("-3")) {
                if (bill.status != Bill.FINISH) {
                    bill.superOrderId = sysorderid;
                    bill.tradeTime = new Timestamp(new SimpleDateFormat("YYYY/MM/DD hh:mm:ss").parse(systime).getTime());
                    bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
                    bill.status = YfbBill.FINISH;
                    //todo msg暂时先记录实际交易金额/元 两位小数
                    bill.msg = ovalue;
                    billMapper.updateByPrimaryKeySelective(bill);

                    //钱包金额变动。
                    walletService.incr(bill.mchId, bill.money - bill.payCharge);
                }

            } else {
                bill.superOrderId = sysorderid;
                bill.tradeTime = new Timestamp(new SimpleDateFormat("YYYY/MM/DD hh:mm:ss").parse(systime).getTime());
                bill.status = YfbBill.FAIL;
                //todo msg暂时先记录实际交易金额/元 两位小数
                bill.msg = ovalue;
                //bill.msg = msg;
                billMapper.updateByPrimaryKeySelective(bill);
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
                return "opstate=0";
            } else {
                //隐藏真实返回.待商户系统返回success再做正确处理
                return "";
            }
        } else {
            return "opstate=-2";
        }
    }
}
