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

    private static Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    AsyncMessageMapper asyncMessageMapper;

    public void addNotifyJobs(Long billId, String url, String params) {
        AsyncMessage asyncMessage = new AsyncMessage();
        asyncMessage.billId = billId;
        asyncMessage.url = url;
        asyncMessage.params = params;
        asyncMessageMapper.insertSelective(asyncMessage);
    }

    private AtomicBoolean isRunning = new AtomicBoolean(false);

    //@Transactional
    public void autoSendNotify() {
        if (!isRunning.compareAndSet(false, true)) {
            LOGGER.info("async send message is running, please waiting");
            return;
        }
        try {
            List<AsyncMessage> list = asyncMessageMapper.selectAll();
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
}
