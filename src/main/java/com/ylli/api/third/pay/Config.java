package com.ylli.api.third.pay;

import com.ylli.api.base.exception.ErrorCode;
import javax.servlet.http.HttpServletResponse;

public class Config {

    /**
     * 模块编号
     */
    public static final int MODEL_CODE = 8;
    /**
     * 错误定义
     */

    public static final ErrorCode ERROR_PERMISSION_DENY
            = new ErrorCode(HttpServletResponse.SC_FORBIDDEN, MODEL_CODE,
            1, "权限不足");

    public static final ErrorCode ERROR_ORDERNO_NOT_EMPTY
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            2, "订单号不能为空");

    public static final ErrorCode ERROR_ORDER_NOT_FOUND
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            3, "订单号不存在");

    public static final ErrorCode ERROR_SERVER_CONNECTION
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            4, "服务连通失败");
}
