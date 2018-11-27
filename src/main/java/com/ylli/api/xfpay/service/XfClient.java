package com.ylli.api.xfpay.service;

import com.ucf.sdk.UcfForOnline;
import com.ylli.api.xfpay.model.XfPaymentRequest;
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

    @Value("${xf.pay.url}")
    public String url;

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
     * @param merchantNo  订单编号
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

        XfPaymentRequest request = new XfPaymentRequest();

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
        request.noticeUrl = "http://47.99.180.135:8080/xfpay/notify";
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
            response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<Object>(reqData, headers), String.class);
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
            response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<Object>(reqData, headers), String.class);
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
}