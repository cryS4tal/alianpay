package com.ylli.api.mch;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.util.ServiceUtil;
import com.ylli.api.mch.model.MchSub;
import com.ylli.api.mch.service.MchSubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Auth
@RestController
@RequestMapping("/mch/sub")
public class MchSubController {

    @Autowired
    MchSubService mchSubService;

    //TODO 权限控制
    //TODO


    @PostMapping
    public void addSub(@RequestBody MchSub mchSub) {
        ServiceUtil.checkNotEmptyIgnore(mchSub, true,"alipayRate","wxRate","bankRate");
        mchSubService.addSub(mchSub.mchId, mchSub.subId, mchSub.type);
    }
}
