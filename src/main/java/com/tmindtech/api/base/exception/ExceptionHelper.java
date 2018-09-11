package com.tmindtech.api.base.exception;

import com.google.gson.Gson;
import com.tmindtech.api.model.base.ErrorMessage;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.springframework.web.client.RestClientResponseException;

/**
 * Created by RexQian on 2017/2/12.
 */
public class ExceptionHelper {

    public static String getStackTrace(Exception ex) {
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        String[] lines = writer.toString().split("\n");
        StringBuilder sb = new StringBuilder();
        boolean hitFlag = false;
        for (String line : lines) {
            if (line.contains("com.tmindtech.api")) {
                hitFlag = true;
            } else if (hitFlag) {
                break;
            }
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    public static AwesomeException from(RestClientResponseException ex) {
        Gson gson = new Gson();
        ErrorMessage errorMessage = gson.fromJson(ex.getResponseBodyAsString(),
                ErrorMessage.class);
        return new AwesomeException(ex.getRawStatusCode(),
                errorMessage.code, errorMessage.message);
    }
}
