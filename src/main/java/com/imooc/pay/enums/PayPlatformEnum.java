package com.imooc.pay.enums;

import com.lly835.bestpay.enums.BestPayTypeEnum;
import lombok.Getter;

@Getter
public enum PayPlatformEnum {
    //`pay_platform` int DEFAULT NULL COMMENT '支付平台:1-支付宝,2-微信',
    ALIPAY(1),

    WX(2),
    ;
    Integer code;

    PayPlatformEnum(Integer code){
        this.code=code;
    }
    //作为静态方法可以方便调用
    public static PayPlatformEnum getByBestPayTypeEnum(BestPayTypeEnum bestPayTypeEnum){
        //这样写比较冗余所以需要改进
//        if(bestPayTypeEnum.getPlatform().name().equals(PayPlatformEnum.ALIPAY.name())){
//            return PayPlatformEnum.ALIPAY;
//        }else if (bestPayTypeEnum.getPlatform().name().equals(PayPlatformEnum.WX.name())){
//            return PayPlatformEnum.WX;
//        }
        //for循环快捷键是PayPlatformEnum.values().for
        for (PayPlatformEnum payPlatformEnum : PayPlatformEnum.values()) {
            if (bestPayTypeEnum.getPlatform().name().equals(payPlatformEnum.name()))
                return payPlatformEnum;
        }
        throw new RuntimeException("错误的支付平台："+bestPayTypeEnum.name());
    }
}
