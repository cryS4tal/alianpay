package com.ylli.api.third.pay.service.xianfen;

import com.ucf.sdk.UcfForOnline;
import com.ylli.api.third.pay.modelVo.xianfen.XianFenRequest;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class XfClient {

    @Value("${xf.pay.notify.url}")
    public String notifyUrl;

    @Value("${xf.recharge.notify.url}")
    public String rechargeNotifyUrl;

    @Value("${xf.pay.version}")
    public String version;

    @Value("${xf.pay.merchant_id}")
    public String merchantId;

    @Value("${xf.pay.xf_pub_key}")
    public String xf_pub_key;

    @Value("${xf.pay.mer_pri_key}")
    public String mer_pri_key;

    //币种_固定值：156（表示人民币）
    public static final String transCur = "156";

    @Autowired
    RestTemplate restTemplate;

    /**
     * 单笔代发
     *
     * @param merchantNo  订单编号 = sysOrderId
     * @param amount      金额
     * @param accountNo   银行卡号
     * @param accountName 持卡人姓名
     * @param mobileNo    手机号
     * @param bankNo      银行编码
     * @param userType    用户类型
     * @param accountType 账户类型
     * @param memo        保留域
     * @return
     */
    public String agencyPayment(String merchantNo, Integer amount, String accountNo, String accountName,
                                String mobileNo, String bankNo, Integer userType, Integer accountType, String memo) {

        XianFenRequest request = new XianFenRequest();

        request.merchantNo = merchantNo;
        request.amount = amount;
        request.accountNo = accountNo;
        request.accountName = accountName;
        request.mobileNo = mobileNo;
        request.bankNo = bankNo;
        request.userType = userType;
        request.accountType = accountType;
        request.memo = memo;

        request.transCur = transCur;
        request.noticeUrl = notifyUrl;
        Map<String, String> map = objectToMap(request);

        //System.out.println(new Gson().toJson(map));
        String reqData = null;
        try {
            reqData = UcfForOnline.generateRequest("REQ_WITHDRAW", version, merchantId, map, xf_pub_key,
                    mer_pri_key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(reqData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange("https://mapi.ucfpay.com/gateway.do", HttpMethod.POST, new HttpEntity<Object>(reqData, headers), String.class);
        } catch (Exception ex) {
            ex.getMessage();
        }
        return response.getBody();
    }

    /**
     * 单笔订单查询
     */
    public String orderQuery(String merchantNo) {

        Map<String, String> map = new HashMap<>();
        map.put("merchantNo", merchantNo);
        String reqData = null;
        try {
            reqData = UcfForOnline.generateRequest("REQ_WITHDRAW_QUERY_BY_ID", version, merchantId, map, xf_pub_key,
                    mer_pri_key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange("https://mapi.ucfpay.com/gateway.do", HttpMethod.POST, new HttpEntity<Object>(reqData, headers), String.class);
        } catch (Exception ex) {
            ex.getMessage();
        }
        String str = response.getBody();

        return str;
    }

    public Map objectToMap(Object object) {
        Map<String, String> map = new LinkedHashMap<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field entityField : fields) {
            Field field = null;
            try {
                field = object.getClass().getDeclaredField(entityField.getName());
                map.put(field.getName(), field.get(object) == null ? "" : field.get(object).toString());
            } catch (NoSuchFieldException exception) {
                exception.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * 先锋代付余额查询.
     */
    public String balance() {
        Map<String, String> map = new HashMap<>();
        map.put("merchantId", merchantId);
        String reqData = null;
        try {
            reqData = UcfForOnline.generateRequest("REQ_QUERY_BALANCE", version, merchantId, map, xf_pub_key,
                    mer_pri_key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange("https://mapi.ucfpay.com/gateway.do", HttpMethod.POST, new HttpEntity<Object>(reqData, headers), String.class);
        } catch (Exception ex) {
            ex.getMessage();
        }
        String str = response.getBody();

        return str;
    }

    /**
     * 线下充值
     * @param merchantNo
     * @param amount
     * @param accountNo
     * @param accountName
     * @param recevieBank
     * @param memo
     * @return
     */
    public String offlineRecharge(String merchantNo,Integer amount,String accountNo,String accountName,String recevieBank,String memo) {
        XianFenRequest request = new XianFenRequest();

        request.merchantNo = merchantNo;
        request.amount = amount;
        request.accountNo = accountNo;
        request.accountName = accountName;

        request.recevieBank = recevieBank;

        request.memo = memo;

        request.transCur = transCur;
        request.noticeUrl = rechargeNotifyUrl;
        Map<String, String> map = objectToMap(request);

        String reqData = null;
        try {
            reqData = UcfForOnline.generateRequest("REQ_OFFLINE_RECHARGE", version, merchantId, map, xf_pub_key,
                    mer_pri_key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(reqData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange("https://mapi.ucfpay.com/gateway.do", HttpMethod.POST, new HttpEntity<Object>(reqData, headers), String.class);
        } catch (Exception ex) {
            ex.getMessage();
        }
        return response.getBody();
    }

    /**
     * 代付充值。单笔订单查询
     * @return
     */
    public String rechargeQuery(String merchantNo) {
        XianFenRequest request = new XianFenRequest();

        request.merchantNo = merchantNo;
        Map<String, String> map = objectToMap(request);

        String reqData = null;
        try {
            reqData = UcfForOnline.generateRequest("REQ_RECHARGE_QUERY", version, merchantId, map, xf_pub_key,
                    mer_pri_key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(reqData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange("https://mapi.ucfpay.com/gateway.do", HttpMethod.POST, new HttpEntity<Object>(reqData, headers), String.class);
        } catch (Exception ex) {
            ex.getMessage();
        }
        return response.getBody();
    }
}