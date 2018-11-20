package com.ylli.api.alipay;

import com.ylli.api.base.exception.ErrorCode;
import javax.servlet.http.HttpServletResponse;

public class Config {

    /**
     * 模块编号
     */
    public static final int MODEL_CODE = 3;
    /**
     * 错误定义
     */
    public static final ErrorCode ERROR_EXAMPLE_NOT_FOUND
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            1, "示例不存在");
}
