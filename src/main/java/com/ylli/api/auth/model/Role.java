package com.ylli.api.auth.model;

import java.sql.Timestamp;
import java.util.List;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Created by RexQian on 2017/2/22.
 */
@Table(name = "t_role")
public class Role {
    /**
     * 系统预设角色
     */
    public static final int TYPE_SYSTEM_PRE = 0;

    /**
     * 系统角色
     */
    public static final int TYPE_SYSTEM = 1;

    /**
     * 组织预设角色
     */
    public static final int TYPE_DEPT_PRE = 2;

    /**
     * 组织角色
     */
    public static final int TYPE_DEPT = 3;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /**
     * 0-系统预设角色 1-系统角色 2-部门预设角色 3-部门角色
     */
    public Integer type;

    /**
     * 角色名
     */
    public String name;

    /**
     * 角色描述
     */
    public String description;

    /**
     * 部门Id
     */
    public Long deptId;

    public Timestamp createTime;

    public Timestamp modifyTime;

    /**
     * 权限Id列表
     */
    @Transient
    public List<Long> permissionIdA;

    @Transient
    public List<PermissionModel> permissions;
}
