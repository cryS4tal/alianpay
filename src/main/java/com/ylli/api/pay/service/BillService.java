package com.ylli.api.pay.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import com.ylli.api.model.base.DataList;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.BaseBill;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.model.SumAndCount;
import com.ylli.api.user.service.AppService;
import com.ylli.api.wallet.service.WalletService;
import com.ylli.api.wzpay.model.WzQueryRes;
import com.ylli.api.wzpay.service.WzClient;
import com.ylli.api.yfbpay.model.YfbBill;
import com.ylli.api.yfbpay.service.YfbService;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BillService {

    @Autowired
    YfbService yfbService;

    @Autowired
    BillMapper billMapper;

    @Autowired
    WzClient wzClient;

    @Autowired
    WalletService walletService;

    @Autowired
    AppService appService;

    /**
     * todo 目前账单系统是分离的。第一版先直接查询易付宝账单.
     *
     * @param userId
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

    public Object getBills(Long userId, Integer status, String mchOrderId, String sysOrderId, String payType,
                           String tradeType, Date tradeTime, Date startTime, Date endTime, int offset, int limit) {

        PageHelper.offsetPage(offset, limit);
        Page<YfbBill> page = (Page<YfbBill>) yfbService.getBills(userId, status, mchOrderId, sysOrderId, payType, tradeType, tradeTime, startTime, endTime);

        DataList<BaseBill> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        List<BaseBill> list = new ArrayList();
        for (YfbBill bill : page) {
            BaseBill object = convert(bill);
            list.add(object);
        }
        dataList.dataList = list;
        return dataList;

    }

    private BaseBill convert(YfbBill bill) {
        BaseBill baseBill = new BaseBill();
        baseBill.mchId = bill.userId;
        baseBill.mchOrderId = bill.subNo;
        baseBill.sysOrderId = bill.orderNo;
        baseBill.money = bill.amount;

        //订单手续费 = bill.手续费 （之前时分润）
        if (bill.bonusMoney != null) {
            baseBill.mchCharge = bill.bonusMoney.intValue();
        }
        baseBill.payType = typeToString(bill.payType, bill.tradeType);
        //baseBill.state = bill.status == YfbBill.NEW ? "新订单" : bill.status == YfbBill.ING ? "进行中" : bill.status == YfbBill.FINISH ? "成功" : "失败";
        baseBill.state = YfbBill.statusToString(bill.status);
        if (bill.tradeTime != null) {
            baseBill.tradeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime);
        }
        baseBill.createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.createTime);
        return baseBill;
    }

    public Object getTodayDetail(Long userId) {
        SumAndCount sumAndCount = yfbService.getTodayDetail(userId);
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
    public Bill orderQuery(String sysOrderId) throws Exception {
        Bill bill = new Bill();
        bill.sysOrderId = sysOrderId;
        bill = billMapper.selectOne(bill);

        //todo 根据订单通道类型  bill.channelId 去主动请求不同的订单查询client.
        if (true) {
            System.out.println("wzClinet");

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

        } else {
            System.out.println("进入易付宝查询");

            return null;
        }

    }

    public Bill selectBySysOrderId(String sysOrderId) {
        Bill bill = new Bill();
        bill.sysOrderId = sysOrderId;
        return billMapper.selectOne(bill);
    }
}
