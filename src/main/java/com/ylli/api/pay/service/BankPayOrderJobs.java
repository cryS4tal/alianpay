package com.ylli.api.pay.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BankPayOrderJobs {

    @Autowired
    MessageService messageService;

    @Scheduled(cron = "0 2/5 * * * ?")
    public void autoSendNotify() {
        messageService.autoOrderNotify();
    }
}
