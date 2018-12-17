package com.ylli.api.third.pay.util;

import com.ylli.api.pingan.model.Packets;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);
    public static final int HEAD_LEN_NEW = 222;
    public static final int HEAD_LEN_OLD = 6;
    public static final String CHARSET = "UTF-8";

    public static Packets sendPost(String xmlStr, String reqUrl) {
        Packets packetsReturn = new Packets();
        OutputStream out = null;
        InputStream in = null;
        try {
            byte[] packets = xmlStr.getBytes("UTF-8");
            URL url = new URL(reqUrl);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setConnectTimeout(6000);
            http.setReadTimeout(6000);
            http.setDoOutput(true);
            http.setDoInput(true);
            http.setAllowUserInteraction(false);
            http.setUseCaches(false);
            http.setRequestMethod("POST");
            http.setRequestProperty("content-type", "text/xml; charset=UTF-8");
            http.setRequestProperty("Content-Length", String.valueOf(packets.length));
            http.setRequestProperty("Connection", "close");
            http.setRequestProperty("User-Agent", "sdb client");
            http.setRequestProperty("Accept", "text/xml");
            out = http.getOutputStream();
            out.write(packets);
            out.flush();

            in = http.getInputStream();
            int code = http.getResponseCode();
            if (code != 200) {
                return null;
            }
            Packets packetRep = new Packets();

            byte[] head = new byte[HEAD_LEN_NEW];
            int recvLen = 0;
            while (recvLen < HEAD_LEN_NEW) {
                recvLen = in.read(head, recvLen, HEAD_LEN_NEW - recvLen);
            }

            packetRep.setHead(head);

            int bodyLen = Integer.parseInt(new String(head, 30, 10, CHARSET));
            packetRep.setLen(bodyLen);

            if (bodyLen > 0) {
                byte[] body = new byte[bodyLen];
                recvLen = 0;
                while (recvLen < bodyLen) {
                    recvLen = in.read(body, recvLen, bodyLen - recvLen);
                }
                packetRep.setBody(body);
            }
            return packetRep;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
            }
        }
    }
}
