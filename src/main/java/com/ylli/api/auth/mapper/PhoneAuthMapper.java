package com.ylli.api.auth.mapper;


import com.ylli.api.auth.model.PhoneAuth;
import tk.mybatis.mapper.common.Mapper;

/**
 * Created by ylli on 2018/11/20.
 */
public interface PhoneAuthMapper extends Mapper<PhoneAuth> {
    /*@Select("SELECT t_phone_auth.id FROM t_phone_auth "
            + " JOIN t_real_name_verify ON t_phone_auth.id = t_real_name_verify.id "
            + " WHERE t_phone_auth.phone = #{phone} AND t_real_name_verify.name = #{name}")
    Long selectByNamePhone(@Param("name") String name,
                           @Param("phone") String phone);*/
}
