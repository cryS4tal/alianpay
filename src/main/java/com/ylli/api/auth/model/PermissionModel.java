package com.ylli.api.auth.model;

import java.sql.Timestamp;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by RexQian on 2017/2/22.
 */
@Table(name = "t_permission")
public class PermissionModel {
    /**
     * 组织权限
     */
    public static final int TYPE_DEPT = 0;

    /**
     * 系统权限
     */
    public static final int TYPE_SYSTEM = 1;

    @Id
    public Long id;

    /**
     * 权限名
     */
    public String name;

    /**
     * 权限描述
     */
    public String description;

    /**
     * 0-组织权限 1-系统权限
     */
    public Integer type;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
