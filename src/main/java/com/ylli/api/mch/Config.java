package com.ylli.api.mch;

import com.ylli.api.auth.model.PermissionModel;
import com.ylli.api.base.exception.ErrorCode;
import javax.servlet.http.HttpServletResponse;

public class Config {

    /**
     * 模块编号
     */
    public static final int MODEL_CODE = 9;
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

    public static final ErrorCode ERROR_ILLEGAL_PHONE
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            4, "请输入正确的联系方式.");

    public static final ErrorCode ERROR_AUDIT_PASS
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            5, "审核已通过，如需修改信息请联系客服.");

    public static final ErrorCode ERROR_SETTLEMENT_EMPTY
            = new ErrorCode(HttpServletResponse.SC_FORBIDDEN, MODEL_CODE,
            6, "请先设置结算信息.");

    public static final ErrorCode ERROR_CHARGE_TYPE
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            7, "不支持的结算类型");


    /**
     * 权限
     */
    public static class SysPermission {
        private static final long BASE = MODEL_CODE * 10000 + PermissionModel.TYPE_SYSTEM * 1000;
        /**
         * 管理角色与权限
         */
        public static final long MANAGE_USER_CHARGE = BASE + 1;

        public static final long MANAGE_APP = BASE + 2;

        //审核用户（代付）
        public static final long MANAGE_USER_BASE = BASE + 3;

        public static final long MANAGE_USER_ACCOUNT = BASE + 4;
    }
}
