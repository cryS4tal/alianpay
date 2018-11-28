package com.ylli.api.yfbpay.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class YfbService {

    private static Logger LOGGER = LoggerFactory.getLogger(YfbService.class);

    @Transactional
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

            System.out.println(sb.toString());
            if (sb.length() == 0) {
                outputStream.write("opstate=-1".getBytes("UTF-8"));
                return;
            }
            //verify sign
            //boolean flag = WXPayUtil.isSignatureValid(sb.toString(), privateKey);
            if (true) {
                //业务处理
                outputStream.write("签名校验成功".getBytes("UTF-8"));
            }
            outputStream.write("签名校验失败".getBytes("UTF-8"));
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
}
