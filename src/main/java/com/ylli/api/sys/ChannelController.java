package com.ylli.api.sys;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.sys.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统通道切换.
 */

@RestController
@RequestMapping("/channel")
public class ChannelController {

    @Autowired
    ChannelService channelService;

    @Autowired
    AuthSession authSession;

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
    @Auth(@Permission(Config.SysPermission.MANAGE_CHANNEL))
    public void channelSwitch(@RequestBody Channel channel) {
        channelService.channelSwitch(channel.id, channel.isOpen);
    }

    @GetMapping("/sys")
    @Auth(@Permission(Config.SysPermission.MANAGE_CHANNEL))
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
    @Auth(@Permission(Config.SysPermission.MANAGE_CHANNEL))
    public void mchChannelSwitch(@RequestBody MchChannel mchChannel) {
        channelService.mchChannelSwitch(mchChannel.mchId, mchChannel.channelId);
    }

    @PostMapping("/mch/{id}")
    @Auth
    public void changeChannel(@PathVariable Long id) {
        if (1024 != authSession.getAuthId()) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        channelService.mchChannelSwitch(authSession.getAuthId(), id);
    }
}
