package com.ylli.api.pay.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.mch.mapper.MchBaseMapper;
import com.ylli.api.mch.service.AppService;
import com.ylli.api.model.base.DataList;
import com.ylli.api.pay.Config;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.BaseBill;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.model.SumAndCount;
import com.ylli.api.pay.util.SerializeUtil;
import com.ylli.api.sys.service.ChannelService;
import com.ylli.api.third.pay.model.WzQueryRes;
import com.ylli.api.third.pay.service.WzClient;
import com.ylli.api.third.pay.service.YfbClient;
import com.ylli.api.wallet.service.WalletService;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillService {

    @Autowired
    BillMapper billMapper;

    @Autowired
    WzClient wzClient;

    @Autowired
    YfbClient yfbClient;

    @Autowired
    WalletService walletService;

    @Autowired
    AppService appService;

    @Autowired
    SerializeUtil serializeUtil;

    @Autowired
    MchBaseMapper userBaseMapper;

    @Autowired
    PayService payService;

    @Autowired
    PayClient payClient;

    @Autowired
    ChannelService channelService;

    /**
     * @param mchId
     * @param status
     * @param mchOrderId
     * @param sysOrderId
     * @param payType
     * @param tradeType
     * @param tradeTime
     * @param startTime
     * @param endTime
     * @return
     */

    public Object getBills(Long mchId, Integer status, String mchOrderId, String sysOrderId, String payType,
                           String tradeType, Date tradeTime, Date startTime, Date endTime, int offset, int limit) {

        PageHelper.offsetPage(offset, limit);
        Page<Bill> page = (Page<Bill>) billMapper.getBills(mchId, status, mchOrderId, sysOrderId, payType, tradeType, tradeTime, startTime, endTime);

        DataList<BaseBill> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        List<BaseBill> list = new ArrayList();
        for (Bill bill : page) {
            BaseBill object = convert(bill);
            list.add(object);
        }
        dataList.dataList = list;
        return dataList;

    }

    private BaseBill convert(Bill bill) {
        BaseBill baseBill = new BaseBill();
        baseBill.mchId = bill.mchId;
        baseBill.mchName = Optional.ofNullable(userBaseMapper.selectByMchId(bill.mchId)).map(i -> i.mchName).orElse(null);
        baseBill.mchOrderId = bill.mchOrderId;
        baseBill.sysOrderId = bill.sysOrderId;
        baseBill.money = bill.money;
        baseBill.mchCharge = bill.payCharge;
        baseBill.payType = typeToString(bill.payType, bill.tradeType);
        baseBill.state = bill.status;
        if (bill.tradeTime != null) {
            baseBill.tradeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime);
        }
        baseBill.createTime = bill.createTime;
        baseBill.channel = channelService.getChannelName(bill.channelId);
        return baseBill;
    }

    public Object getTodayDetail(Long mchId) {
        SumAndCount sumAndCount = billMapper.getTodayDetail(mchId);
        if (sumAndCount.total == null) {
            sumAndCount.total = 0L;
        }
        return sumAndCount;
    }

    public String typeToString(String payType, String tradeType) {
        if (Strings.isNullOrEmpty(tradeType)) {
            tradeType = PayService.NATIVE;
        }
        return new StringBuffer()
                .append(payType)
                .append(tradeType)
                .toString()
                .replace(PayService.ALI, "支付宝")
                .replace(PayService.WX, "微信")
                .replace(PayService.NATIVE, "");
    }


    public boolean mchOrderExist(String mchOrderId) {
        Bill bill = new Bill();
        bill.mchOrderId = mchOrderId;
        return billMapper.selectOne(bill) != null;
    }

    public Bill selectByMchOrderId(String mchOrderId) {
        Bill bill = new Bill();
        bill.mchOrderId = mchOrderId;
        return billMapper.selectOne(bill);
    }

    /**
     *
     */
    public Bill orderQuery(String sysOrderId, String code) throws Exception {
        Bill bill = new Bill();
        bill.sysOrderId = sysOrderId;
        bill = billMapper.selectOne(bill);

        if (code.equals("WZ")) {

            WzQueryRes res = wzClient.orderQuery(sysOrderId);
            if (res.code.equals("success")) {
                if (bill.status != Bill.FINISH) {
                    bill.status = Bill.FINISH;
                    bill.tradeTime = Timestamp.from(Instant.now());
                    bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
                    billMapper.updateByPrimaryKeySelective(bill);

                    //钱包金额变动。
                    walletService.incr(bill.mchId, bill.money - bill.payCharge);
                }
            } else if (res.code.equals("fail")) {
                if (bill.status == Bill.NEW) {
                    bill.status = Bill.FAIL;
                    billMapper.updateByPrimaryKeySelective(bill);
                }
            }
            return bill;

        } else if (code.equals("YFB") || code.equals("HRJF")) {

            String str = yfbClient.orderQuery(bill.sysOrderId);
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
            if (flag) {
                if (opstate.equals("0")) {
                    if (bill.status != Bill.FINISH) {
                        bill.status = Bill.FINISH;
                        bill.msg = ovalue;
                        bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
                        bill.tradeTime = Timestamp.from(Instant.now());
                        billMapper.updateByPrimaryKeySelective(bill);

                        //钱包金额变动。
                        walletService.incr(bill.mchId, bill.money - bill.payCharge);
                    }
                }
                //其他情况 暂时不做处理.

            }
            return bill;

        } else {
            return bill;
        }
    }

    public Bill selectBySysOrderId(String sysOrderId) {
        Bill bill = new Bill();
        bill.sysOrderId = sysOrderId;
        return billMapper.selectOne(bill);
    }

    /**
     * 自动关闭创建时间（time_zone=0.00）+ 9h < now()的订单。
     * 超时1小时关闭
     */
    @Transactional
    public void autoClose() {
        billMapper.autoClose();
    }

    @Transactional
    public Bill createBill(Long mchId, String mchOrderId, Long channelId, String payType, String tradeType, Integer money, String reserve, String notifyUrl, String redirectUrl) {
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
        return bill;
    }

    @Transactional
    public Object reissue(String sysOrderId) throws Exception {
        Bill bill = new Bill();
        bill.sysOrderId = sysOrderId;
        bill = billMapper.selectOne(bill);
        if (bill == null) {
            throw new AwesomeException(Config.ERROR_BILL_NOT_FOUND);
        }
        //补单操作..
        if (bill.status == Bill.NEW || bill.status == Bill.AUTO_CLOSE) {
            bill.status = Bill.FINISH;
            bill.tradeTime = Timestamp.from(Instant.now());
            bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;

            //不返回上游订单号.
            bill.superOrderId = new StringBuffer().append("unknown").append(bill.id).toString();
            bill.msg = (new BigDecimal(bill.money).divide(new BigDecimal(100))).toString();
            billMapper.updateByPrimaryKeySelective(bill);

            //钱包金额变动。
            walletService.incr(bill.mchId, bill.money - bill.payCharge);

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
        } else {
            throw new AwesomeException(Config.ERROR_BILL_STATUS);
        }
        return convert(bill);
    }

    @Transactional
    public void rollback(String sysOrderId) {
        Bill bill = new Bill();
        bill.sysOrderId = sysOrderId;
        bill = billMapper.selectOne(bill);
        if (bill == null) {
            throw new AwesomeException(Config.ERROR_BILL_NOT_FOUND);
        }
        if (bill.status != Bill.FINISH ) {
             throw new AwesomeException(Config.ERROR_BILL_STATUS);
        }

        if (!bill.superOrderId.startsWith("unknown")) {
            throw new AwesomeException(Config.ERROR_BILL_ROLLBACK);
        }
        if (bill.status == Bill.FINISH) {
            //钱包金额变动。
            walletService.rollback(bill.mchId, bill.money - bill.payCharge);

            billMapper.rollback(sysOrderId);
        }
    }
}
