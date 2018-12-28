package com.ylli.api.sys.service;

import com.google.gson.Gson;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.sys.model.Data;
import com.ylli.api.sys.model.HourlyData;
import com.ylli.api.sys.model.TotalData;
import com.ylli.api.wallet.mapper.CashLogMapper;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
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
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 8);
        int hour = cal.get(Calendar.HOUR);
        Map<Integer, String> map = new HashMap<>();
        for (int i = 0; i <= hour; i++) {
            int h = i;
            double hourSum = list.stream().filter(item -> item.hour == h).mapToDouble(item -> item.count).sum();
            double successHourSum = list.stream().filter(item -> item.hour == h).filter(item -> item.status == Bill.FINISH).mapToDouble(item -> item.count).sum();
            map.put(i, dividePercent(hourSum, successHourSum));
        }
        return map;
    }

    public String dividePercent(double m, double s) {
        if(m==0 || s==0)
            return "0";
        NumberFormat percent = NumberFormat.getPercentInstance();
        percent.setMaximumFractionDigits(2);
        BigDecimal mother = new BigDecimal(m);
        BigDecimal son = new BigDecimal(s);
        return percent.format(son.divide(mother, 4, BigDecimal.ROUND_UP));
    }
}
