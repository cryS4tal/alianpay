package com.ylli.api.wzpay.service;

import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.util.SerializeUtil;
import com.ylli.api.user.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WzService {

    @Autowired
    WzClient wzClient;

    @Autowired
    SerializeUtil serializeUtil;

    @Autowired
    BillMapper billMapper;

    @Autowired
    AppService appService;

    @Value("${pay.wz.notify}")
    public String notify;

    @Value("${pay.wz.id}")
    public String spid;

    @Value("${pay.wz.secret}")
    public String secret;

    @Transactional
    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, String payType, String tradeType, Object extra) throws Exception {
        Bill bill = new Bill();
        bill.mchId = mchId;
        bill.sysOrderId = serializeUtil.generateSysOrderId();
        bill.mchOrderId = mchOrderId;
        bill.channelId = channelId;
        // todo 应用模块 关联.
        bill.appId = appService.getAppId(payType, tradeType);

        bill.money = money;
        bill.status = Bill.NEW;
        bill.reserve = reserve;
        bill.notifyUrl = notifyUrl;
        bill.redirectUrl = redirectUrl;
        bill.payType = payType;
        bill.tradeType = tradeType;
        billMapper.insertSelective(bill);

        String mz = String.format("%.2f", (money / 100.0));
        //商品名称 = 商户号
        return wzClient.createWzOrder(bill.sysOrderId, mz, reserve, mchId.toString(), redirectUrl, mchId.toString());
    }


}
