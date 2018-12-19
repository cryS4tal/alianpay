package com.ylli.api.auth.mapper;

import com.ylli.api.auth.model.Account;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

/**
 * Created by ylli on 2018/11/20.
 */
public interface AccountMapper extends Mapper<Account> {

    List<Account> getAccounts(@Param("name_like") String nameLike);

    /**
     * 新增账户全局搜索。
     */

    List<Account> selectByCondition(@Param("mch_id") Long mchId,
                                    @Param("mch_name") String mchName);
}
