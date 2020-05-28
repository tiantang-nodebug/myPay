package com.imooc.pay.impl;

import com.imooc.pay.PayApplicationTests;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import org.junit.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class PayServiceTest extends PayApplicationTests {

    @Autowired
    private PayService payService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void creat() {
        /**
         * payService.creat()第二个参数有两种写法 以180.0的价格来说明
         * 第一种方法是BigDecimal.valueOf(180.0)  这种方式是先把180.0 double型改成string再得到BigDecimal类型
         * 第二种方法是 new BigDecimal("180.0")   千万不要用new BigDecimal(180.0)，因为精度会出现问题
         */
        payService.create("37822971728463", BigDecimal.valueOf(0.01), BestPayTypeEnum.WXPAY_NATIVE);//第二个参数有两种写法，第一种写法是该行的写法
    }


    @Test
    public void sendMQMsg(){
        amqpTemplate.convertAndSend("payNotify","Hello");
    }

}