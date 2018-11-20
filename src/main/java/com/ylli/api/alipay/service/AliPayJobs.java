package com.ylli.api.alipay.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by ylli on 2018/11/20.
 */

@Component
@ConditionalOnProperty("pay.jobs.enable")
@EnableAsync
public class AliPayJobs {
    @Autowired
    AliPayService aliPayService;

    @Scheduled(cron = "0/1 * * * * ?")
    public void autoOrderQuery() {
        aliPayService.autoSendNotify();
    }
}
