package com.test.alipay.dao;

import com.test.alipay.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeDao extends JpaRepository<Trade, Integer> {

    Trade findByOutTradeNo(String outTradeNo);

}
