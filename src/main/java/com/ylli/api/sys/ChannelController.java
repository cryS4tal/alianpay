package com.ylli.api.sys;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.sys.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/sys/channel")
public class ChannelController {

    @Autowired
    ChannelService channelService;

    static class Channel {
        public Long id;
        public Boolean isOpen;
    }

    @PostMapping
    public void channelSwitch(@RequestBody Channel channel) {
        channelService.channelSwitch(channel.id, channel.isOpen);
    }

}
