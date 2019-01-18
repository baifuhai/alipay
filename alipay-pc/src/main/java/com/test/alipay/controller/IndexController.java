package com.test.alipay.controller;

import com.test.alipay.dao.TradeDao;
import com.test.alipay.model.Trade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    TradeDao tradeDao;

    @RequestMapping("/")
    public String index(Map<String, Object> map) {
        List<Trade> tradeList = tradeDao.findAll();
        map.put("tradeList", tradeList);
        return "index";
    }
    
}
