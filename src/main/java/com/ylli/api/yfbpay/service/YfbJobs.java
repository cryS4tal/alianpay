package com.ylli.api.yfbpay.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class YfbJobs {

    @Autowired
    YfbService yfbService;

    // todo  加入超时关闭是否会对订单逻辑产生影响 需要确认.

    //@Scheduled(cron = "0 0/10 * * * ?")
    //public void autoCloseIouJobs() {
    //    yfbService.closeExpiredBill();
    //}

}
