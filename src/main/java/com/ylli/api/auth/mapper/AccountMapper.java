package com.ylli.api.auth.mapper;

import com.ylli.api.auth.model.Account;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/**
 * Created by ylli on 2018/11/20.
 */
public interface AccountMapper extends Mapper<Account> {

    List<Account> getAccounts(@Param("name_like") String nameLike);

    /*Integer selectByTime(@Param("start") String startTime, @Param("end") String endTime);

    List<AccountInfo> queryByCondition(@Param("id") Long id,
                                       @Param("nickname") String nickname,
                                       @Param("state") String state,
                                       @Param("name") String name,
                                       @Param("card_id") String cardId,
                                       @Param("phone") String phone);

    @Update("UPDATE t_account SET nickname = #{nickname},avatar = #{avatar} WHERE id = ${id}")
    void removeWechatInfo(@Param("id") Long accountId,
                          @Param("nickname") String nickname,
                          @Param("avatar") String avatar);

    @Select("SELECT * FROM t_account WHERE id >= ${m} AND state = #{state} LIMIT ${n}")
    List<Account> selectByState(@Param("state") String state,
                                @Param("m") int offset,
                                @Param("n") int limit);

    @Select("SELECT COUNT(*) FROM t_account WHERE state = #{state}")
    Long selectByStateCount(@Param("state") String state);*/
}
