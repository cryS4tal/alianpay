package com.ylli.api.pay.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BillJobs {

    @Autowired
    BillService billService;

    @Autowired
    MessageService messageService;

    /**
     * 每10分钟轮询.
     * 超时20分钟关闭
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void autoClose() {
        billService.autoClose();
    }


    @Scheduled(cron = "0 0/5 * * * ?")
    public void autoSendNotify() {
        messageService.autoSendNotify();
    }
}
