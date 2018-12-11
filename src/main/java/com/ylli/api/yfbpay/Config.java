package com.ylli.api.yfbpay;

import com.ylli.api.auth.model.PermissionModel;
import com.ylli.api.base.exception.ErrorCode;
import javax.servlet.http.HttpServletResponse;

public class Config {

    /**
     * 模块编号
     */
    public static final int MODEL_CODE = 12;
    /**
     * 错误定义
     */
    public static final ErrorCode ERROR_PERMISSION_DENY
            = new ErrorCode(HttpServletResponse.SC_FORBIDDEN, MODEL_CODE,
            1, "权限不足");

    public static final ErrorCode ERROR_SERVER_CONNECTION
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            2, "服务连通失败");

}
