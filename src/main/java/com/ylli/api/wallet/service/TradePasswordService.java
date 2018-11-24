package com.ylli.api.wallet.service;

import com.ylli.api.wallet.mapper.TradePasswordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TradePasswordService {

    @Autowired
    TradePasswordMapper tradePasswordMapper;
}
