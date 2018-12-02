package com.ylli.api.pay.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.model.base.DataList;
import com.ylli.api.pay.model.BaseBill;
import com.ylli.api.pay.model.SumAndCount;
import com.ylli.api.yfbpay.model.YfbBill;
import com.ylli.api.yfbpay.service.YfbService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BillService {

    @Autowired
    YfbService yfbService;

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
        baseBill.payType = bill.payType;
        baseBill.state = bill.status == YfbBill.NEW ? "新订单" : bill.status == YfbBill.ING ? "进行中" : bill.status == YfbBill.FINISH ? "成功" : "失败";
        if (bill.tradeTime != null) {
            baseBill.tradeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime);
        }

        return null;
    }

    public Object getTodayDetail(Long userId) {
        SumAndCount sumAndCount = yfbService.getTodayDetail(userId);
        return sumAndCount;
    }
}
