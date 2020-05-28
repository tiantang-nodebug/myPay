package com.imooc.pay.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data //lombok插件
public class PayInfo {
    private Integer id;

    private Integer userId;

    private Long orderNo;

    private Integer payPlatform;
    //带参构造方法，其实在业务中userId是非常必要的。但是本课程简化就只用订单号。日期交给数据库完成，会自动设置日期。platformNumber也不写，所以不加
    public PayInfo(Long orderNo, Integer payPlatform, String platformStatus, BigDecimal payAmount) {
        this.orderNo = orderNo;
//        this.payPlatform = payPlatform;
        this.platformNumber = platformNumber;
        this.platformStatus = platformStatus;
        this.payAmount = payAmount;
    }

    private String platformNumber;

    private String platformStatus;

    private BigDecimal payAmount;

    private Date createTime;

    private Date updateTime;

}
