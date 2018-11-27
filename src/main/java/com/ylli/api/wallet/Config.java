package com.ylli.api.wallet;

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
    public static final ErrorCode ERROR_WALLET_ERROR
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            1, "钱包数据丢失");

    public static final ErrorCode ERROR_BILL_NOT_FOUND
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            2, "账单不存在");
}
