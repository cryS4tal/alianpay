package com.ylli.api.pay.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import com.ylli.api.model.base.DataList;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.BaseBill;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.model.SumAndCount;
import com.ylli.api.third.pay.model.WzQueryRes;
import com.ylli.api.third.pay.model.YfbBill;
import com.ylli.api.third.pay.service.WzClient;
import com.ylli.api.third.pay.service.YfbClient;
import com.ylli.api.third.pay.service.YfbService;
import com.ylli.api.user.mapper.UserBaseMapper;
import com.ylli.api.user.service.AppService;
import com.ylli.api.wallet.service.WalletService;
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
    YfbService yfbService;

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
    BillService billService;

    @Autowired
    UserBaseMapper userBaseMapper;

    /**
     * todo 目前账单系统是分离的。第一版先直接查询易付宝账单.
     *
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
        baseBill.state = Bill.statusToString(bill.status);
        if (bill.tradeTime != null) {
            baseBill.tradeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime);
        }
        baseBill.createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.createTime);
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

        //todo  test 根据订单通道类型  bill.channelId 去主动请求不同的订单查询client.
        if (code.equals("WZ")) {
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

        } else if (code.equals("YFB")) {
            System.out.println("进入易付宝查询");

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
                        bill.status = YfbBill.FINISH;
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
            return null;
        }
    }

    public Bill selectBySysOrderId(String sysOrderId) {
        Bill bill = new Bill();
        bill.sysOrderId = sysOrderId;
        return billMapper.selectOne(bill);
    }

    /**
     * 自动关闭创建时间（time_zone=0.00）+ 10h < now()的订单。
     * 超时2小时关闭
     */
    @Transactional
    public void autoClose() {
        billMapper.autoClose();
    }
}
