package com.ylli.api.mch;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.base.util.ServiceUtil;
import com.ylli.api.mch.model.MchAgency;
import com.ylli.api.mch.service.MchAgencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Auth(@Permission(Config.SysPermission.MANAGE_AGENCY))
@RestController
@RequestMapping("/mch/agency")
public class MchAgencyController {

    @Autowired
    MchAgencyService mchAgencyService;

    @PostMapping
    public void addSub(@RequestBody MchAgency mchAgency) {
        ServiceUtil.checkNotEmptyIgnore(mchAgency, true, "alipayRate", "wxRate", "bankRate");
        mchAgencyService.addSub(mchAgency.mchId, mchAgency.subId, mchAgency.type);
    }

    @GetMapping
    public Object agencyList(@AwesomeParam Integer type,
                             @AwesomeParam(required = false) Long mchId,
                             @AwesomeParam(required = false) Long subId,
                             @AwesomeParam(defaultValue = "0") int offset,
                             @AwesomeParam(defaultValue = "10") int limit) {
        return mchAgencyService.agencyList(type, mchId, subId, offset, limit);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        mchAgencyService.delete(id);
    }
}
