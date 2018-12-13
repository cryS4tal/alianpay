package com.ylli.api.wzpay.service;


import com.google.gson.Gson;
import com.ylli.api.wallet.mapper.WzCashLogMapper;
import com.ylli.api.wallet.model.WzCashLog;
import com.ylli.api.wallet.model.WzRes;
import com.ylli.api.wallet.service.CashService;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class WzCashJobs {

    @Value("${pay.wz.fail.count}")
    public Integer failCount;

    private static Logger LOGGER = LoggerFactory.getLogger(WzCashJobs.class);

    @Autowired
    WzCashLogMapper wzCashLogMapper;

    @Autowired
    WzClient wzClient;

    @Autowired
    CashService cashService;

    private AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * 自动轮询提现请求（网众支付）
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    @Transactional
    public void autoQuery() {
        if (!isRunning.compareAndSet(false, true)) {
            LOGGER.info("auto query is running, please waiting");
            return;
        }

        Gson gson = new Gson();
        List<WzCashLog> logs = wzCashLogMapper.selectAll();

        try {
            logs.stream().forEach(item -> {
                if (item.failCount > failCount) {
                    cashService.success(item.logId, false);
                    wzCashLogMapper.delete(item);
                } else {
                    try {
                        String str = wzClient.cashRes(item.logId.toString());
                        if (str == null) {
                            return;
                        }
                        WzRes wzRes = gson.fromJson(str, WzRes.class);
                        if (wzRes.code.equals("200")) {
                            cashService.success(item.logId, true);
                            wzCashLogMapper.delete(item);
                        } else {
                            item.failCount = item.failCount + 1;
                            item.errcode = wzRes.code;
                            item.errmsg = wzRes.msg;
                            wzCashLogMapper.updateByPrimaryKeySelective(item);
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } finally {
            isRunning.set(false);
        }
    }
}
