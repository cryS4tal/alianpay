package com.ylli.api.pay.mapper;

import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.model.CategoryData;
import com.ylli.api.sys.model.Data;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface BillMapper extends Mapper<Bill> {

    List<Bill> getBills(@Param("mch_ids") List<Long> mchIds,
                        @Param("status") Integer status,
                        @Param("mch_order_id") String mchOrderId,
                        @Param("sys_order_id") String sysOrderId,
                        @Param("pay_type") String payType,
                        @Param("trade_time") Date tradeTime,
                        @Param("start_time") Date startTime,
                        @Param("end_time") Date endTime);

    @Update("UPDATE t_bill SET status = 9 WHERE status = 1 AND DATE_ADD(create_time,INTERVAL 20 MINUTE) < NOW()")
    Integer autoClose();

    List<Data> getHourlyData(@Param("mch_id") Long mchId);

    List<Data> getDayData(@Param("mch_id") Long mchId);

    Long selectTotalMoney(@Param("mch_id") Long mchId);

    Long selectChargeMoney(@Param("mch_id") Long mchId);

    List<Data> rate(@Param("channel_id") Long channelId,
                    @Param("mch_id") Long mchId,
                    @Param("app_id") Long appId);

    @Update("UPDATE t_bill SET `status` = 1,trade_time = NULL,pay_charge = NULL,super_order_id = NULL,msg = NULL,is_success = NULL WHERE sys_order_id = #{sys_order_id}")
    void rollback(@Param("sys_order_id") String sysOrderId);

    List<Bill> getOrders(@Param("auth_id") Long authId,
                         @Param("nick_name") String nickName,
                         @Param("phone") String phone,
                         @Param("status") Integer status,
                         @Param("sys_order_id") String sysOrderId,
                         @Param("mch_order_id") String mchOrderId,
                         @Param("start_time") Date startTime,
                         @Param("end_time") Date endTime);

    List<CategoryData> category(@Param("channel_id") Long channelId,
                                @Param("mch_id") Long mchId,
                                @Param("status") Integer status,
                                @Param("time") Date time,
                                @Param("groupby") Integer groupby);
}
