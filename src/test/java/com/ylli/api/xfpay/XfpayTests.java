package com.ylli.api.xfpay;

import com.google.gson.Gson;
import com.ucf.sdk.UcfForOnline;
import com.ylli.api.third.pay.modelVo.xianfen.Data;
import com.ylli.api.third.pay.modelVo.xianfen.XianFenResponse;
import com.ylli.api.third.pay.service.xianfen.XfClient;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by ylli on 2017/4/18.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class XfpayTests {

    @Value("${xf.pay.mer_pri_key}")
    public String mer_pri_key;

    @Autowired
    XfClient client;

    @Test
    public void createOrder() throws Exception {
        String str = client.agencyPayment("XianFen_test003", 100, "6217920274920375",
                "李玉龙", "", "12", 1, 1, "");

        XianFenResponse response = new Gson().fromJson(str, XianFenResponse.class);
        //加密后的业务数据
        String bizData = UcfForOnline.decryptData(str, mer_pri_key);

        /**
         * 99000 - 接口调用成功
         * 99001 - 接口调用异常
         * 其他返回码，接口调用失败，可置订单为失败
         */
        if (response.code.equals("99000")) {
            //交易成功返回订单数据
            Data data = new Gson().fromJson(bizData, Data.class);

            //应答码，00000 成功
            if (data.resCode.equals("00000")) {

                //交易订单号
                String tradeNo = data.tradeNo;
                //交易时间
                String tradeTime = data.tradeTime;

                if (data.status != null && data.status.toUpperCase().equals("S")) {

                    return;
                }
                if (data.status != null && data.status.toUpperCase().equals("F")) {
                    return;
                }
                if (data.status != null && data.status.toUpperCase().equals("I")) {
                    return;
                }
            }
            //应答失败，返回先锋的业务返回码 + 描述

        } else if (response.code.equals("99001")) {

            //return getResJson(response.code, response.message, null);
        } else {
            //通用错误返回.
            //具体原因 @see 先锋支付网关返回码.
            //对下游服务商隐藏先锋返回message，统一返回请求失败
            //return getResJson(response.code, "请求失败", null);
        }
    }

    @Test
    public void orderQuery() {
        String s = client.orderQuery("111");
        System.out.println(s);
    }
}
