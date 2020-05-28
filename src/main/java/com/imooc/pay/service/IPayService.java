package com.imooc.pay.service;

import com.imooc.pay.pojo.PayInfo;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;

import java.math.BigDecimal;


public interface IPayService {

    /**
     * 发起支付
     */
    PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum);

    /**
     * 异步通知处理
     * @param notifyData
     * @return
     */
    String asyncNotify(String notifyData);

    /**
     *查询支付记录（通过订单号）
     * @param orderId
     * @return
     */
    PayInfo queryByOrderId(String orderId);
}
