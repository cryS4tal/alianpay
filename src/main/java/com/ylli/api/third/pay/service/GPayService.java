package com.ylli.api.third.pay.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import static com.ucf.sdk.util.RSAUtils.KEY_ALGORITHM;
import com.ylli.api.mch.service.RateService;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import com.ylli.api.pay.service.PayClient;
import com.ylli.api.pay.service.PayService;
import com.ylli.api.third.pay.model.GPNotify;
import com.ylli.api.wallet.service.WalletService;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.ResourceAccessException;

@Service
public class GPayService {

    private static Logger LOGGER = LoggerFactory.getLogger(GPayService.class);

    @Autowired
    BillService billService;

    @Autowired
    BillMapper billMapper;

    @Autowired
    GPayClient gPayClient;

    @Autowired
    PayService payService;

    @Autowired
    PayClient payClient;

    @Autowired
    WalletService walletService;

    @Autowired
    RateService appService;

    @Value("${gpay.public.key}")
    public String publicKey;


    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, String payType, String tradeType, Object extra) {

        //创建订单
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);
        try {
            String result = gPayClient.createOrder(bill.money, payType);
            /**
             * { "code": 5,
             *   "data": {},
             *   "message": "设备1389忙碌 设备1397不在线 设备1379忙碌 设备1386忙碌 ",
             *   "sign": ""}
             */
            JSONObject jsonObject = JSON.parseObject(result);

            String code = jsonObject.getString("code");

            if ("1".equals(code)) {
                //下单成功
                String data = jsonObject.getString("data");
                Map<String, Object> map = JSON.parseObject(data);
                String sign = jsonObject.getString("sign");
                Boolean success = verifyMap(map, sign);
                if (success) {
                    //更新上游订单号。
                    bill.superOrderId = map.get("orderId").toString();
                    billMapper.updateByPrimaryKeySelective(bill);
                }
                return map.get("payUrl").toString();
            }
            LOGGER.error("create gpay order fail: mchOrderId = " + mchOrderId + jsonObject.getString("message"));
            return new StringBuffer("message").append(jsonObject.getString("message")).toString();
        } catch (ResourceAccessException exception) {
            return new StringBuffer("message").append("请求超时").toString();
        }
    }


    public Boolean verifyMap(Map map, String sign) {
        Boolean success = false;
        try {
            if (!map.isEmpty()) {
                Set keySet = map.keySet();
                String[] keyArray = (String[]) keySet.toArray(new String[keySet.size()]);
                Arrays.sort(keyArray);
                StringBuffer buffer = new StringBuffer();
                for (String key : keyArray) {
                    /**
                     * 如果为空就不加入签名
                     */
                    String value = map.get(key) == null ? "" : map.get(key).toString();
                    if (StringUtils.isNotBlank(value)) {
                        buffer.append(key + "=" + value).append("&");
                    }
                }
                /**
                 * 重新排序后的字符串
                 */
                String str = buffer.toString().toUpperCase();
                success = verify(str.getBytes(), publicKey, sign);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public boolean verify(byte[] data, String publicKey, String sign)
            throws Exception {
        byte[] keyBytes = Base64Utils.decode(publicKey.getBytes());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicK = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initVerify(publicK);
        signature.update(data);
        return signature.verify(Base64Utils.decode(sign.getBytes()));
    }

    @Transactional
    public void paynotify(GPNotify notify) throws Exception {

        String data = new Gson().toJson(notify.data);
        //不使用 objectToMap ,因为这里是驼峰命名，而系统是驼峰转下划线
        Map<String, Object> map = JSON.parseObject(data);
        String sign = notify.sign;
        Boolean success = verifyMap(map, sign);

        if (success) {
            Bill bill = billService.selectBySuperOrderId(notify.data.orderId);
            if (bill == null) {
                throw new RuntimeException("order 404 not found");
            }
            if (bill.status == Bill.FINISH || bill.status == Bill.FAIL) {
                throw new RuntimeException("order finish");
            }

            if ("END".equals(notify.data.status)) {
                if (bill.status != Bill.FINISH) {
                    bill.tradeTime = new Timestamp(System.currentTimeMillis());
                    bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
                    bill.status = Bill.FINISH;
                    bill.msg = String.valueOf(Integer.parseInt(notify.data.actualMoney) / 100);
                    billMapper.updateByPrimaryKeySelective(bill);

                    //钱包金额变动。
                    walletService.incr(bill.mchId, bill.money, bill.payCharge, bill.payType);
                }

            } else {
                bill.tradeTime = new Timestamp(System.currentTimeMillis());
                bill.status = Bill.FAIL;
                bill.msg = notify.data.actualMoney;
                //bill.msg = msg;
                billMapper.updateByPrimaryKeySelective(bill);
            }

            //加入异步通知下游商户系统
            //params jsonStr.
            if (!Strings.isNullOrEmpty(bill.notifyUrl)) {
                String params = payService.generateRes(
                        bill.money.toString(),
                        bill.mchOrderId,
                        bill.sysOrderId,
                        bill.status == Bill.FINISH ? "S" : bill.status == Bill.FAIL ? "F" : "I",
                        bill.tradeTime == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime),
                        bill.reserve);

                payClient.sendNotify(bill.id, bill.notifyUrl, params, true);
            }
        } else {
            throw new RuntimeException("sign error");
        }
    }
}
