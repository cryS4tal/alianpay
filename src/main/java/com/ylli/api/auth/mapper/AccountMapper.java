package com.ylli.api.auth.mapper;

import com.ylli.api.auth.model.Account;
import com.ylli.api.mch.model.Mch;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

/**
 * Created by ylli on 2018/11/20.
 */
public interface AccountMapper extends Mapper<Account> {

    /**
     * 新增账户全局搜索。
     */
    List<Mch> selectByQuery(@Param("phone") String phone,
                            @Param("mch_id") String mchId,
                            @Param("mch_name") String mchName,
                            @Param("audit_state") Integer auditState,
                            @Param("mch_state") String mchState);
}
