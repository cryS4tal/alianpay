package com.ylli.api.third.pay.service;

import com.google.gson.Gson;
import com.ylli.api.wallet.mapper.SysPaymentLogMapper;
import com.ylli.api.wallet.model.SysPaymentLog;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableAsync
public class PingAnJobs {

    @Autowired
    PingAnService pingAnService;

    @Autowired
    SysPaymentLogMapper sysPaymentLogMapper;

    private static Logger LOGGER = LoggerFactory.getLogger(PingAnJobs.class);

    private AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * 自动轮询提现请求（平安代付）
     */
    @Scheduled(cron = "0 0/3 * * * ?")
    //@Transactional
    public void autoQuery() {
        if (!isRunning.compareAndSet(false, true)) {
            LOGGER.info("pingan auto query is running, please waiting");
            return;
        }

        try {
            //每分钟查询一次.. 最多查询10次
            List<SysPaymentLog> logs = sysPaymentLogMapper.selectAll();
            if (logs.size() == 0) {
                return;
            }
            logs.stream().forEach(item -> {
                if (item.failCount > 10) {
                    sysPaymentLogMapper.delete(item);
                } else {
                    if (item.type.equals(SysPaymentLog.TYPE_MCH)) {
                        //sys auto query.
                        pingAnService.payQuery(item);
                    } else if (item.type.equals(SysPaymentLog.TYPE_SYS)) {
                        //mch auto query.
                        pingAnService.payQueryMch(item);
                    } else {
                        System.out.println(new Gson().toJson(item));
                        System.out.println("商户自动查询未处理");
                    }
                }
            });
        } finally {
            isRunning.set(false);
        }
    }
}
