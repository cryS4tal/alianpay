package com.ylli.api.sys.service;

import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.mch.mapper.MchAgencyMapper;
import com.ylli.api.mch.model.MchAgency;
import com.ylli.api.pay.Config;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.model.CategoryData;
import com.ylli.api.sys.model.Bonus;
import com.ylli.api.sys.model.Data;
import com.ylli.api.sys.model.HourlyData;
import com.ylli.api.sys.model.TotalData;
import com.ylli.api.wallet.mapper.CashLogMapper;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticsService {

    @Autowired
    BillMapper billMapper;

    @Autowired
    CashLogMapper cashLogMapper;

    @Autowired
    MchAgencyMapper mchAgencyMapper;

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

    /**
     * date
     * day week month year
     */
    public Object category(Long channelId, Long mchId, Integer status, String date, Integer groupby) {
        List<CategoryData> list = billMapper.category(channelId, mchId, status, createTime(date), groupby);
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

    /**
     * 系统分润统计.
     *
     * @param mchId
     * @return
     */
    public Object bonus(Long mchId, String date) {
        //今日分润. 历史分润.
        List<Bonus> list = new ArrayList<>();
        //获得所有的代理商.
        List<MchAgency> agencies = mchAgencyMapper.selectAll();
        if (mchId != null) {
            agencies = agencies.stream().filter(item -> item.mchId == mchId).collect(Collectors.toList());
        }

        //去重
        List<Long> longs = agencies.stream().map(i -> i.mchId).distinct().collect(Collectors.toList());

        for (int i = 0; i < longs.size(); i++) {
            Long mch = longs.get(i);

            Bonus bonus = new Bonus();

            /*bonus.money = agencies.stream().filter(item -> item.mchId == mch).mapToLong(item -> {
                if (item.type == 1) {
                    //支付
                    //return billMapper.bonus();
                } else {
                    //代付
                    //return item.bankRate;
                }
            }).sum();*/
            list.add(bonus);
        }
        return list;
    }
}
