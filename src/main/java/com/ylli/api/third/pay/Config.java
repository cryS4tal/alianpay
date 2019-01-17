package com.ylli.api.third.pay;

import com.ylli.api.auth.model.PermissionModel;
import com.ylli.api.base.exception.ErrorCode;
import javax.servlet.http.HttpServletResponse;

public class Config {

    /**
     * 模块编号
     */
    public static final int MODEL_CODE = 10;
    /**
     * 错误定义
     */

    public static final ErrorCode ERROR_PERMISSION_DENY
            = new ErrorCode(HttpServletResponse.SC_FORBIDDEN, MODEL_CODE,
            1, "权限不足");

    public static final ErrorCode ERROR_URL_EXIST
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            2, "收款码已存在");

    public static final ErrorCode ERROR_QR_CODE_NOT_FOUND
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            3, "资源不存在");

    public static final ErrorCode ERROR_SERVER_CONNECTION
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            4, "服务连通失败");

    public static final ErrorCode ERROR_UID_EXIST
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            5, "uid已存在");

    /**
     * 权限
     */
    public static class SysPermission {
        private static final long BASE = MODEL_CODE * 10000 + PermissionModel.TYPE_SYSTEM * 1000;
        /**
         * 管理角色与权限
         */
        public static final long MANAGE_QR_CODE = BASE + 1;

        public static final long QR_CODE = BASE + 2;
    }
}
