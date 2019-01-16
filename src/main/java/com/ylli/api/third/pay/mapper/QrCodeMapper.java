package com.ylli.api.third.pay.mapper;

import com.ylli.api.third.pay.model.QrCode;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

public interface QrCodeMapper extends Mapper<QrCode> {

    List<QrCode> selectByCondition(@Param("auth_id") Long authId,
                                   @Param("nick_name") String nickName,
                                   @Param("phone") String phone);
}
