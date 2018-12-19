package com.ylli.api.pay.service;

import com.ylli.api.pay.mapper.AsyncMessageMapper;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.AsyncMessage;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.third.pay.service.WzClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Component
public class PayClient {
    private static Logger LOGGER = LoggerFactory.getLogger(WzClient.class);

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    BillMapper billMapper;

    @Autowired
    AsyncMessageMapper asyncMessageMapper;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String sendNotify(Long id, String notifyUrl, String params, Boolean first) {
        LOGGER.info("send mch notify:" + id + "\n params:" + params);
        String res = null;
        try {
            res = restTemplate.postForObject(notifyUrl, params, String.class);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("received mch res:" + res);

        if (res.toUpperCase().equals("SUCCESS")) {
            Bill bill = billMapper.selectByPrimaryKey(id);
            if (bill != null) {
                bill.isSuccess = true;
                billMapper.updateByPrimaryKeySelective(bill);
            }
        }
        /**
         * first = true，res != success
         * 加入异步消息通知
         */
        if (first && !res.toUpperCase().equals("SUCCESS")) {
            AsyncMessage asyncMessage = new AsyncMessage();
            asyncMessage.billId = id;
            asyncMessage.url = notifyUrl;
            asyncMessage.params = params;
            asyncMessageMapper.insertSelective(asyncMessage);

        } else if (!first && !res.toUpperCase().equals("SUCCESS")) {
            AsyncMessage message = new AsyncMessage();
            message.billId = id;
            message = asyncMessageMapper.selectOne(message);
            if (message.failCount > 5) {
                asyncMessageMapper.delete(message);
            } else {
                message.failCount = message.failCount + 1;
                asyncMessageMapper.updateByPrimaryKeySelective(message);
            }
        }
        return res;
    }
}
