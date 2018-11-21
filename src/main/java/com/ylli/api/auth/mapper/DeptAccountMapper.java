package com.ylli.api.auth.mapper;

import com.ylli.api.auth.model.Account;
import com.ylli.api.auth.model.Department;
import com.ylli.api.auth.model.DeptAccount;
import com.ylli.api.auth.model.Role;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

/**
 * Created by RexQian on 2017/2/22.
 */
public interface DeptAccountMapper extends Mapper<DeptAccount> {

    List<Account> getAccountList(@Param("dept_id") Long deptId,
                                 @Param("name_like") String nameLike,
                                 @Param("role_id_list") List<Long> roleId);

    List<Department> getDeptList(@Param("account_id") long accountId);

    List<Role> getRoleList(@Param("account_id") Long accountId,
                           @Param("dept_id") Long deptId);
}
