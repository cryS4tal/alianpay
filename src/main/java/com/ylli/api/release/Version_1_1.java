package com.ylli.api.release;

import com.ylli.api.auth.mapper.AccountMapper;
import com.ylli.api.auth.model.Account;
import com.ylli.api.sys.mapper.MchChannelMapper;
import com.ylli.api.sys.model.MchChannel;
import com.ylli.api.sys.service.ChannelService;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用于v1.1 版本更新纠正数据.
 */
@Component
public class Version_1_1 {

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    MchChannelMapper mchChannelMapper;

    @Autowired
    ChannelService channelService;


    @PostConstruct
    void init() {
        //channel();
    }

    /**
     * 1.1 版本数据纠正..
     * 对以存在商户插入默认通道.  网众
     */
    /*void channel() {
        List<Account> accounts = accountMapper.selectAll();
        accounts.stream().forEach(i -> {
            MchChannel channel = new MchChannel();
            channel.mchId = i.id;
            channel = mchChannelMapper.selectOne(channel);
            if (channel == null) {
                channelService.channelInit(i.id, 2L);
            }
        });
    }*/
}
