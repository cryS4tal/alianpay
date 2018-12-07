package com.ylli.api.phone;

import com.ylli.api.base.exception.ErrorCode;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by RexQian on 2017/4/21.
 */
public class Config {

    /**
     * 模块编号
     */
    public static final int MODEL_CODE = 7;

    /**
     * 错误定义
     */

    public static final ErrorCode ERROR_INVALID_PHONE
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            1, "无效的手机号码");

    public static final ErrorCode ERROR_OUT_OF_FREQ
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            2, "验证码请求过于频繁");

    public static final ErrorCode ERROR_INVALID_CODE
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            3, "无效的验证码");

    public static final ErrorCode ERROR_EXPIRED_CODE
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            4, "验证码过期或失效, 请重新发送");

    public static final ErrorCode ERROR_CODE
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            5, "验证码错误");

    public static final ErrorCode ERROR_CODE_FORMAT
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            6, "验证码错误。%s");

    public static final ErrorCode ERROR_SEND_VERIFY_CODE
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            7, "发送验证码失败");

    public static final ErrorCode ERROR_VERIFY
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            8, "%s");

    public static final ErrorCode ERROR_SERVER
            = new ErrorCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MODEL_CODE,
            9, "服务器错误");


}
