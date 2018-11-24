package com.ylli.api.user;

import com.ylli.api.auth.model.PermissionModel;
import com.ylli.api.base.exception.ErrorCode;
import javax.servlet.http.HttpServletResponse;

public class Config {

    /**
     * 模块编号
     */
    public static final int MODEL_CODE = 5;
    /**
     * 错误定义
     */
    public static final ErrorCode ERROR_PERMISSION_DENY
            = new ErrorCode(HttpServletResponse.SC_FORBIDDEN, MODEL_CODE,
            1, "权限不足");

    public static final ErrorCode ERROR_USER_NOT_FOUND
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            2, "用户不存在");

    public static final ErrorCode ERROR_APP_NOT_FOUND
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            3, "应用不存在");

    public static final ErrorCode ERROR_APP_IN_USERD
            = new ErrorCode(HttpServletResponse.SC_FORBIDDEN, MODEL_CODE,
            4, "应用正在使用中");

    public static final ErrorCode ERROR_USER_TYPE
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            5, "用户类型不正确");
    /**
     * 权限
     */
    public static class SysPermission {
        private static final long BASE = MODEL_CODE * 10000 + PermissionModel.TYPE_SYSTEM * 1000;
        /**
         * 管理角色与权限
         */
        public static final long MANAGE_USER_CHARGE = BASE + 1;

        public static final long MANAGE_USER_APP = BASE + 2;
    }
}
