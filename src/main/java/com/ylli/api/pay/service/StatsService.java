package com.ylli.api.pay.service;

import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.pay.Config;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.model.CategoryData;
import com.ylli.api.sys.model.Data;
import com.ylli.api.sys.model.HourlyData;
import com.ylli.api.sys.model.TotalData;
import com.ylli.api.wallet.mapper.CashLogMapper;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    @Autowired
    BillMapper billMapper;

    @Autowired
    CashLogMapper cashLogMapper;

    public Object hourlyData(Long mchId) {

        List<Data> list = billMapper.getHourlyData(mchId);
        HourlyData data = new HourlyData();
        data.snew = list.stream().filter(item -> item.status == Bill.NEW).collect(Collectors.toList());
        data.sclose = list.stream().filter(item -> item.status == Bill.AUTO_CLOSE).collect(Collectors.toList());
        data.sfinish = list.stream().filter(item -> item.status == Bill.FINISH).collect(Collectors.toList());
        data.sfail = list.stream().filter(item -> item.status == Bill.FAIL).collect(Collectors.toList());
        data.sing = list.stream().filter(item -> item.status == Bill.ING).collect(Collectors.toList());
        return list;
    }

    public Object total(Long mchId) {
        TotalData totalData = new TotalData();

        List<Data> list = billMapper.getDayData(mchId);

        totalData.smoney = list.stream().filter(item -> item.status == Bill.FINISH).map(item -> item.sum).reduce(0L, (a, b) -> a + b);
        totalData.scount = list.stream().filter(item -> item.status == Bill.FINISH).mapToInt(i -> i.count).sum();

        totalData.fmoney = list.stream()
                .filter(item -> item.status == Bill.AUTO_CLOSE || item.status == Bill.FAIL)
                .map(item -> item.sum).reduce(0L, (a, b) -> a + b);
        totalData.fcount = list.stream()
                .filter(item -> item.status == Bill.AUTO_CLOSE || item.status == Bill.FAIL)
                .mapToInt(i -> i.count).sum();

        totalData.tmoney = Optional.ofNullable(billMapper.selectTotalMoney(mchId)).orElse(0L);
        totalData.cmoney = Optional.ofNullable(cashLogMapper.selectCashMoney(mchId)).orElse(0L);
        Long charge = Optional.ofNullable(billMapper.selectChargeMoney(mchId)).orElse(0L);

        totalData.rmoney = totalData.tmoney - totalData.cmoney - charge;

        return totalData;
    }

    public Object successRate(Long channelId, Long mchId, Long appId) {
        List<Data> list = billMapper.rate(channelId, mchId, appId);


        return null;
    }

    /**
     * date
     * day week month year
     */
    public Object category(Long channelId, Long mchId, String status, String date) {
        List<CategoryData> list = billMapper.category(channelId, mchId, status, createTime(date));
        return list;
    }

    public Date createTime(String date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        //todo 是否-8h.

        if ("0d".equals(date)) {
            return calendar.getTime();
        }
        if ("1d".equals(date)) {
            int day = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.set(Calendar.DAY_OF_YEAR, day - 1);
            return calendar.getTime();
        }
        if ("3d".equals(date)) {
            int day = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.set(Calendar.DAY_OF_YEAR, day - 3);
            return calendar.getTime();
        }
        if ("7d".equals(date)) {
            int day = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.set(Calendar.DAY_OF_YEAR, day - 7);
            return calendar.getTime();
        }
        if ("1m".equals(date)) {
            int month = calendar.get(Calendar.MONTH);
            calendar.set(Calendar.MONTH, month - 1);
            return calendar.getTime();
        }
        if ("3m".equals(date)) {
            int month = calendar.get(Calendar.MONTH);
            calendar.set(Calendar.MONTH, month - 3);
            return calendar.getTime();
        }
        if ("1y".equals(date)) {
            int year = calendar.get(Calendar.YEAR);
            calendar.set(Calendar.YEAR, year - 1);
            return calendar.getTime();
        }
        throw new AwesomeException(Config.ERROR_DATE);
    }
}
