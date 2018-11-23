package com.ylli.api.storage;

import com.ylli.api.base.exception.ErrorCode;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by RexQian on 2017/3/14.
 */
public class Config {

    /**
     * 模块编号
     */
    public static final int MODEL_CODE = 6;


    /**
     * 错误定义
     */

    public static final ErrorCode ERROR_EMPTY_FILE
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            1, "文件不能为空");


    public static final ErrorCode ERROR_SAVE_FILE
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            2, "保存文件失败。%s");

    public static final ErrorCode ERROR_FILE_NOT_FOUND
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            3, "文件不存在");

    public static final ErrorCode ERROR_FILE_NOT_READ
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            4, "文件无法读取。%s");
}
