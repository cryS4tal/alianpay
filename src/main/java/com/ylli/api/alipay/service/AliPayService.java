package com.ylli.api.alipay.service;

import com.google.gson.Gson;
import com.ylli.api.alipay.mapper.NotifyMapper;
import com.ylli.api.alipay.model.Notify;
import com.ylli.api.alipay.model.OrderNotifyRes;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@EnableAsync
public class AliPayService {

    private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AliPayService.class);

    @Autowired
    AliPayClient aliPayClient;

    @Autowired
    NotifyMapper notifyMapper;

    //todo 加入用户登陆与自己业务系统逻辑
    public Object createAliPayOrder() {

        return null;
    }

    public void payNotify(HttpServletRequest request, HttpServletResponse response) {
        InputStream inputStream;
        OutputStream outputStream = null;
        try {
            //读取参数
            StringBuffer sb = new StringBuffer();
            inputStream = request.getInputStream();
            String s;
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while ((s = in.readLine()) != null) {
                sb.append(s);
            }
            in.close();
            inputStream.close();
            outputStream = response.getOutputStream();
            if (sb.length() == 0) {
                outputStream.write(new String("failed").getBytes("UTF-8"));
                return;
            }

            //verify sign
            boolean flag = aliPayClient.isSignatureValid(sb.toString());
            if (flag) {
                //todo 业务逻辑处理


                // important 加入自己的回调通知接口
                // params 参数参加快易支付需要定义
                Notify notify = new Notify();
                aliPayClient.sendNotify(notify);


                outputStream.write(new String("success").getBytes("UTF-8"));
            }
            outputStream.write(new String("failed").getBytes("UTF-8"));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 定义回调返回json.
     */
    public String getResJson(String code, String msg) {
        OrderNotifyRes ret = new OrderNotifyRes();
        ret.code = code;
        ret.message = msg;
        return new Gson().toJson(ret);
    }

    public void autoSendNotify() {
        List<Notify> list = notifyMapper.selectNotify();
        for (int i = 0; i < list.size(); i++) {
            Notify notify = list.get(i);
            aliPayClient.sendNotify(notify);
        }
    }
}
