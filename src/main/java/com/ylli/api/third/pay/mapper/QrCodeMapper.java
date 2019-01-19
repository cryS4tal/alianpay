package com.ylli.api.third.pay.mapper;

import com.ylli.api.third.pay.model.QrCode;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface QrCodeMapper extends Mapper<QrCode> {

    List<QrCode> selectByCondition(@Param("auth_id") Long authId,
                                   @Param("nick_name") String nickName,
                                   @Param("phone") String phone);

    @Select("SELECT * FROM t_qr_code WHERE auth_id = ${auth_id}")
    QrCode selectByAuthId(@Param("auth_id") Long authId);

    @Select("SELECT * FROM t_qr_code WHERE code_url = #{url}")
    QrCode selectByUrl(@Param("url") String url);

    @Select("SELECT DISTINCT auth_id FROM t_qr_code WHERE `enable` = 1")
    List<QrCode> selectLoginCount();

}
