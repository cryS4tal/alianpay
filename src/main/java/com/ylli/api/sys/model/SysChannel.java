package com.ylli.api.sys.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Table(name = "t_sys_channel")
public class SysChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String code;

    public String name;

    public Boolean state;

    public Timestamp createTime;

    public Timestamp modifyTime;

    public static String getName(@NotNull Long channelId) {
        //不修改数据库的情况下直接本地读取
        //db修改记得增加解析
        if (channelId == 1) {
            return "易付宝";
        }
        if (channelId == 2) {
            return "网众";
        }
        if (channelId == 3) {
            return "个码";
        }
        if (channelId == 4) {
            return "个码-风控";
        }
        if (channelId == 5) {
            return "CNT支付";
        }
        if (channelId == 6) {
            return "畅通支付";
        }
        return "未知";
    }
}
