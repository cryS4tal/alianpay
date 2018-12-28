package com.ylli.api.pay.service;

import com.ylli.api.pay.mapper.AsyncMessageMapper;
import com.ylli.api.pay.mapper.BankPayOrderMapper;
import com.ylli.api.pay.model.AsyncMessage;
import com.ylli.api.pay.model.BankPayOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Component
public class BankPayClient {
    private static Logger LOGGER = LoggerFactory.getLogger(BankPayClient.class);

    @Value("${notify.limit}")
    public Integer notifyLimit;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    BankPayOrderMapper bankPayOrderMapper;

    @Autowired
    AsyncMessageMapper asyncMessageMapper;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String sendNotify(String sysOrderId, String notifyUrl, String params, Boolean first) {
        LOGGER.info("send bank pay notify:" + sysOrderId + "\n params:" + params);
        String res = null;
        try {
            res = restTemplate.postForObject(notifyUrl, params, String.class);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("received mch res:" + res);

        if (res == null) {
            return null;
        }

        if (res.toUpperCase().equals("SUCCESS")) {
            BankPayOrder payOrder = bankPayOrderMapper.selectBySysOrderId(sysOrderId);
            //Bill bill = billMapper.selectByPrimaryKey(id);
            if (payOrder != null) {
                payOrder.isSuccess = true;
                bankPayOrderMapper.updateByPrimaryKeySelective(payOrder);
            }
        }
        /**
         * first = true，res != success
         * 加入异步消息通知
         */
        if (first && !res.toUpperCase().equals("SUCCESS")) {
            AsyncMessage asyncMessage = new AsyncMessage();
            asyncMessage.bankPayOrderId = sysOrderId;
            asyncMessage.url = notifyUrl;
            asyncMessage.params = params;
            asyncMessageMapper.insertSelective(asyncMessage);

        } else if (!first && !res.toUpperCase().equals("SUCCESS")) {
            AsyncMessage message = new AsyncMessage();
            message.bankPayOrderId = sysOrderId;
            message = asyncMessageMapper.selectOne(message);
            if (message.failCount > notifyLimit) {
                //TODO 暂时先删除。后续是否保留？加入状态控制
                asyncMessageMapper.delete(message);
            } else {
                message.failCount = message.failCount + 1;
                message.errmsg = res;
                asyncMessageMapper.updateByPrimaryKeySelective(message);
            }
        }
        return res;
    }
}
