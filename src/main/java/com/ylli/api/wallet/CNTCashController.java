package com.ylli.api.wallet;

import com.ylli.api.wallet.model.CNTAuth;
import com.ylli.api.wallet.model.CNTCash;
import com.ylli.api.wallet.model.CNTQuery;
import com.ylli.api.wallet.service.CNTCashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用于cnt 通道
 * <p>
 * 多账户授权合并余额提现..
 */
@RestController
@RequestMapping("/cash/v1")
public class CNTCashController {

    @Autowired
    CNTCashService cntCashService;

    @PostMapping("/convert")
    public Object convert(@RequestBody CNTAuth auth) throws Exception {
        return cntCashService.convert(auth);
    }

    @PostMapping
    public Object cash(@RequestBody CNTCash cntCash) throws Exception {
        return cntCashService.cash(cntCash);
    }

    @PostMapping("/query")
    public Object query(@RequestBody CNTQuery query) throws Exception {
        return cntCashService.query(query);
    }
}
