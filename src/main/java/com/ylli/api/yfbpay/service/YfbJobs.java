package com.ylli.api.yfbpay.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class YfbJobs {

    @Autowired
    YfbService yfbService;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void autoCloseIouJobs() {
        yfbService.closeExpiredBill();
    }

}
