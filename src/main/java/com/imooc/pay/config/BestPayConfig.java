package com.imooc.pay.config;

import com.lly835.bestpay.config.AliPayConfig;
import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.service.BestPayService;
import com.lly835.bestpay.service.impl.BestPayServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class BestPayConfig {
    @Autowired
    private WxaccountConfig wxaccountConfig;

    @Autowired
    private AliAccountConfig aliAccountConfig;

    @Bean
    //现在只是写了一个方法，需要加注释，这样就会在项目启动时执行。注释见上方
    public BestPayService bestPayService(WxPayConfig wxPayConfig){
//        //实现支付配置

        //下面的微信配置好之后，在PayController不好获取returnUrl。所以需要另写一个Bean,将下面代码复制到那个里面
        //但是这样会导致下面的wxPayConfig不能识别，所以解决办法可以加@Autowire 或者是在该函数传参WxPayConfig wxPayConfig
//        //可根据支付方式来选择参数设置
//        //以下选择微信的Native支付方式，所以目前appId、商户Id、商户密钥必须要（特别提醒证书在发起支付是不需要的）
//        //微信配置
        //WxPayConfig wxPayConfig=new WxPayConfig();
//        wxPayConfig.setAppId("wxd898fcb01713c658");//appId
//        wxPayConfig.setMchId("1483469312");
//        wxPayConfig.setMchKey("098F6BCD4621D373CADE4E832627B4F6");
//        wxPayConfig.setNotifyUrl("http://imooc-malllearn.natapp1.cc/pay/notify");//接收支付平台异步通知的地址，先随便写一个比如http：//127.0.0.1
//        wxPayConfig.setReturnUrl("https://www.imooc.com/");//微信跳转地址
        //支付宝配置
        AliPayConfig aliPayConfig = new AliPayConfig();
        aliPayConfig.setAppId(aliAccountConfig.getAppid());
        aliPayConfig.setPrivateKey(aliAccountConfig.getPrivateKey());
        aliPayConfig.setAliPayPublicKey(aliAccountConfig.getApaliPayPublicKeypid());
        aliPayConfig.setReturnUrl(aliAccountConfig.getReturnUrl());//支付之后，支付宝跳转的地址，暂时可写主页地址http://127.0.0.1
        aliPayConfig.setNotifyUrl(aliAccountConfig.getNotifyUrl());

        BestPayServiceImpl bestPayService = new BestPayServiceImpl();//因为已经Autowire
        bestPayService.setWxPayConfig(wxPayConfig);
        bestPayService.setAliPayConfig(aliPayConfig);
        return  bestPayService;
    }
    @Bean
    public WxPayConfig wxPayConfig(){
        WxPayConfig wxPayConfig=new WxPayConfig();
//        wxPayConfig.setAppId("wxd898fcb01713c658");//appId
//        wxPayConfig.setMchId("1483469312");
//        wxPayConfig.setMchKey("098F6BCD4621D373CADE4E832627B4F6");
//        wxPayConfig.setNotifyUrl("http://imooc-malllearn.natapp1.cc/pay/notify");//接收支付平台异步通知的地址，先随便写一个比如http：//127.0.0.1
//        wxPayConfig.setReturnUrl("https://127.0.0.1");//微信跳转地址
        wxPayConfig.setAppId(wxaccountConfig.getAppid());//appId
        wxPayConfig.setMchId(wxaccountConfig.getMchId());
        wxPayConfig.setMchKey(wxaccountConfig.getMchKey());
        wxPayConfig.setNotifyUrl(wxaccountConfig.getNotifyUrl());//接收支付平台异步通知的地址，先随便写一个比如http：//127.0.0.1
        wxPayConfig.setReturnUrl(wxaccountConfig.getReturnUrl());//微信跳转地址
        return wxPayConfig;
    }
}
