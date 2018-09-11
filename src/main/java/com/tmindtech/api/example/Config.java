package com.tmindtech.api.example;

import com.tmindtech.api.base.exception.ErrorCode;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by RexQian on 2017/5/10.
 */
public class Config {
    /**
     * 模块编号
     */
    public static final int MODEL_CODE = 2;
    /**
     * 错误定义
     */
    public static final ErrorCode ERROR_EXAMPLE_NOT_FOUND
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            1, "示例不存在");
    public static final ErrorCode ERROR_EMPTY_USERNAME
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            2, "示例名字不能为空");
}
