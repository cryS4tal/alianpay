package com.ylli.api.wzpay;

import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.wzpay.service.WzClient;
import com.ylli.api.wzpay.service.WzService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay/wz")
public class WzpayController {

    @Autowired
    WzService wzService;

    @Autowired
    WzClient wzClient;

    @GetMapping("/notify")
    public String payNotify(@AwesomeParam Long spid,
                            @AwesomeParam String md5,
                            @AwesomeParam String oid,
                            @AwesomeParam String sporder,
                            @AwesomeParam String mz,
                            @AwesomeParam String zdy,
                            @AwesomeParam Long spuid) throws Exception {
        return wzService.payNotify(spid, md5, oid, sporder, mz, zdy, spuid);
    }

    /**
     * 测试api
     */
    @GetMapping("/test")
    public String dotest() throws Exception {
        return wzClient.cash("2018121209305200000003");
    }

    /**
     * 自己组装参数 向网众发起提现请求。
     */
    @GetMapping("/cash")
    public String sendCash(@AwesomeParam String account,
                           @AwesomeParam String card,
                           @AwesomeParam String name,
                           @AwesomeParam String fullname,
                           @AwesomeParam String linked,
                           @AwesomeParam String money,
                           @AwesomeParam String cityid,
                           @AwesomeParam String idcard,
                           @AwesomeParam String mobile,
                           @AwesomeParam String sysOrderId) throws Exception {
        return wzClient.cash(account, card, name, fullname, linked, money, cityid, idcard, mobile, sysOrderId);
    }


    @GetMapping("/test1")
    public String dotest1() throws Exception {
        return wzClient.cashRes("2018121202593000000001");
    }
}