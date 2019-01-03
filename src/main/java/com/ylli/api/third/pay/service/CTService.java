package com.ylli.api.third.pay.service;

import com.google.gson.Gson;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import com.ylli.api.third.pay.model.CTOrderResponse;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CTService {

    private static Logger LOGGER = LoggerFactory.getLogger(CTService.class);

    @Autowired
    BillService billService;

    @Autowired
    BillMapper billMapper;

    @Autowired
    CTClient ctClient;

    @Transactional
    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, String payType, String tradeType, Object extra) throws Exception {
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);

        String total_fee = (new BigDecimal(money).divide(new BigDecimal(100))).toString();

        String str = ctClient.createOrder(total_fee, bill.sysOrderId);
        CTOrderResponse response = new Gson().fromJson(str, CTOrderResponse.class);

        if (!response.result) {
            LOGGER.error("ct order fail: "
                    + "\n mch_order_id : " + mchOrderId + "  sys_order_id : " + bill.sysOrderId
                    + "\n res : " + str);
            //更新订单为失败。
            bill.status = Bill.FAIL;
            billMapper.updateByPrimaryKeySelective(bill);
            return null;
        }
        return response.data;
    }
}
