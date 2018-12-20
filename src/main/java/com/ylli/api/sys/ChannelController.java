package com.ylli.api.sys;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.sys.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统通道切换.
 * <p>
 * v1.0 先支持整个系统通道切换。后续可以加入不同用户通道选择.
 */
@Auth
@RestController
@RequestMapping("/channel")
public class ChannelController {

    @Autowired
    ChannelService channelService;

    static class Channel {
        public Long id;
        public Boolean isOpen;
    }

    /**
     * 开启关闭系统通道
     *
     * @param channel
     */
    @PostMapping("/sys")
    public void channelSwitch(@RequestBody Channel channel) {
        channelService.channelSwitch(channel.id, channel.isOpen);
    }

    @GetMapping("/sys")
    public Object sysChannels(@AwesomeParam(defaultValue = "0") int offset,
                              @AwesomeParam(defaultValue = "20") int limit) {
        return channelService.sysChannels(offset, limit);
    }

    static class MchChannel {
        public Long mchId;
        public Long channelId;
    }

    /**
     * 切换商户通道
     *
     * @param mchChannel
     */
    @PostMapping("/mch")
    public void mchChannelSwitch(@RequestBody MchChannel mchChannel) {
        channelService.mchChannelSwitch(mchChannel.mchId, mchChannel.channelId);
    }

}
