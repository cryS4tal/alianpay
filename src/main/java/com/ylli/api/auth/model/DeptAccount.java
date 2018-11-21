package com.ylli.api.auth.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by RexQian on 2017/2/22.
 */
@Table(name = "t_dept_account")
public class DeptAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long accountId;

    public Long roleId;

    /**
     * 部门Id 冗余字段
     */
    public Long deptId;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
