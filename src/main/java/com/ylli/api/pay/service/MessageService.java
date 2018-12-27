package com.ylli.api.pay.service;

import com.ylli.api.pay.mapper.AsyncMessageMapper;
import com.ylli.api.pay.model.AsyncMessage;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    @Autowired
    PayClient payClient;

    @Autowired
    BankPayClient bankPayClient;

    private static Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    AsyncMessageMapper asyncMessageMapper;

    private AtomicBoolean isRunning = new AtomicBoolean(false);

    //@Transactional
    public void autoSendNotify() {
        if (!isRunning.compareAndSet(false, true)) {
            LOGGER.info("async send message is running, please waiting");
            return;
        }
        try {
            List<AsyncMessage> list = asyncMessageMapper.selectAllBill();
            for (AsyncMessage message : list) {
                try {
                    //
                    String res = payClient.sendNotify(message.billId, message.url, message.params, false);
                } catch (Exception ex) {
                    continue;
                }
            }
        } finally {
            isRunning.set(false);
        }
    }

    private AtomicBoolean isRunning2 = new AtomicBoolean(false);

    public void autoOrderNotify() {
        if (!isRunning2.compareAndSet(false, true)) {
            LOGGER.info("async send message is running, please waiting");
            return;
        }
        try {
            List<AsyncMessage> list = asyncMessageMapper.selectAllOrder();
            for (AsyncMessage message : list) {
                try {
                    //
                    String res = bankPayClient.sendNotify(message.bankPayOrderId, message.url, message.params, false);
                } catch (Exception ex) {
                    continue;
                }
            }
        } finally {
            isRunning2.set(false);
        }
    }
}
