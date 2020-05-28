package com.imooc.pay.impl;

import com.google.gson.Gson;
import com.imooc.pay.dao.PayInfoMapper;
import com.imooc.pay.enums.PayPlatformEnum;
import com.imooc.pay.pojo.PayInfo;
import com.imooc.pay.service.IPayService;
import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.enums.OrderStatusEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Component
@Service
public class PayService implements IPayService {

    @Autowired
    private BestPayService bestPayService;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private final static String QUEUE_PAY_NOTIFY="payNotify";


    /**
     * 写入数据库
     *发起支付
     * @param orderId
     * @param amount
     * @param bestPayTypeEnum  目前只支持两种方式，所以可以加判断约束一下
     */
    @Override
    //因为要加入支付宝的支付方式，所以需要添加个参数：支付方式
    public PayResponse create(String orderId, BigDecimal amount,BestPayTypeEnum bestPayTypeEnum) {
        /*
        写入数据库
        数据库里的orderId是long类型，而本类里是String类型。需要转换一下。推荐使用String
        payPlatform弄成枚举类型。新建一个enums文件夹，创建PayPlatform
        platformStatus是枚举类型，bestpay-sdk已经写好了。ctrl——右键com.lly835.bestpay.enums.BestPayTypeEnum的enums就可以看到OrderStatusEnum
         */
        PayInfo payInfo= new PayInfo(Long.parseLong(orderId), PayPlatformEnum.getByBestPayTypeEnum(bestPayTypeEnum).getCode(), OrderStatusEnum.NOTPAY.name(),amount);
        //如果要串参数，需要写很多set方法。所以可以创建一个带参构造方法
        //可以查看insert方法是完整的sql查询语句。所以用insertSelective方法
//        payInfoMapper.insert(payInfo);
        payInfoMapper.insertSelective(payInfo);

        //以下注释掉的代码在BestPayConfig里
//        //微信支付配置  见best-pay-sdk的使用文档
//        WxPayConfig wxPayConfig=new WxPayConfig();
//        //可根据支付方式来选择参数设置
//        //以下选择微信的Native支付方式，所以目前appId、商户Id、商户密钥必须要（特别提醒证书在发起支付是不需要的）
//        //支付商户资料
//        wxPayConfig.setAppId("wxd898fcb01713c658");//appId
//        wxPayConfig.setMchId("1483469312");
//        wxPayConfig.setMchKey("098F6BCD4621D373CADE4E832627B4F6");
//        wxPayConfig.setNotifyUrl("http://imooc-malllearn.natapp1.cc/pay/notify");//接收支付平台异步通知的地址，先随便写一个比如http：//127.0.0.1
//        //BestPayServiceImpl bestPayService = new BestPayServiceImpl();//因为已经Autowire
//        bestPayService.setWxPayConfig(wxPayConfig);

        //还缺一段代码是写入数据库
        /*
              if (bestPayTypeEnum==BestPayTypeEnum.WXPAY_NATIVE || bestPayTypeEnum==BestPayTypeEnum.ALIPAY_PC){
            //实现支付功能代码
        }
        这样写，功能代码块就被写在里面，且若支持的支付方式越多，判断语句越长。所以换一种思路相反的方式来判断
         */
//        if (bestPayTypeEnum!=BestPayTypeEnum.WXPAY_NATIVE && bestPayTypeEnum!=BestPayTypeEnum.ALIPAY_PC){
//            throw new RuntimeException("暂不支持的支付类型");
//        }
        PayRequest request =new PayRequest();
        request.setOrderName("8498166-Java资料");//订单名是 慕课网uid-
        request.setOrderId(orderId);//赋值Id
        request.setOrderAmount(amount.doubleValue());//金额
        //request.setPayTypeEnum(BestPayTypeEnum.WXPAY_NATIVE);//支付方式Native支付方式
        //BestPayTypeEnum是支付方式，枚举类型,为方便加入不同的支付方式，所以
        request.setPayTypeEnum(bestPayTypeEnum);
        PayResponse response = bestPayService.pay(request);

        //在该类添加注释@Slf4j后使用日志打印
        log.info("发起支付通知 response={}",response);

        return response;
    }

    /**
     * 异步通知处理
     *
     * @param notifyData
     * @return
     */
    @Override
    public String asyncNotify(String notifyData) {
        /*
        1.签名校验 bestPayService的sdk已经做好了
        因为在create函数new了一个bestPayService。所以要进行改进。在两个函数外部Autowire
         */
        PayResponse payResponse = bestPayService.asyncNotify(notifyData);
        log.info("异步通知 payesponse={}",payResponse);

        //2.金额校验（从数据库查订单）
        PayInfo payInfo = payInfoMapper.selectByOrderNo(Long.parseLong(payResponse.getOrderId()));

        if(payInfo==null){
            //这种情况比较严重(正常情况下不会发生)，需要发出告警：钉钉、短信
            /*
            最好加入告警步骤
             */
            //抛出异常
            throw new RuntimeException("数据异常：通过订单号查询得到的结果是null");
        }
//        if (payInfo.getPlatformStatus().equals(OrderStatusEnum.SUCCESS.name()){
//            /*
//            //4 支付成功后，告诉平台不要再重复发送异步通知了
//            这样会导致代码重复不建议这么使用，所以可以反向思维。
//             */
//        }
        //如果支付状态未支付成功
        if (!payInfo.getPlatformStatus().equals(OrderStatusEnum.SUCCESS.name())){
            //用BigDecimal类型来比较。因为double类型存在精度问题
            if (payInfo.getPayAmount().compareTo(BigDecimal.valueOf(payResponse.getOrderAmount()))!=0){
                /*
                最好加入告警步骤
                 */
                //抛出异常。当出现这种情况千万不要在日志只写一句话。因为以后万一出现这种情况要排查问题
                throw new RuntimeException("异步通知里的金额和数据库里的金额不一致,orderNo"+payResponse.getOrderId());
            }
            //3.在签名校验和金额校验都完成后，需要修改订单支付状态(更新数据库)
            payInfo.setPlatformStatus(OrderStatusEnum.SUCCESS.name());
            payInfo.setPlatformNumber(payResponse.getOutTradeNo());//获取交易流水号，由支付平台产生
            //设置数据库的更新时间，有两种方式，第一种是下行所示。第二种是删除update的关于update_time的相关sql语句
            payInfo.setUpdateTime(null);
            payInfoMapper.updateByPrimaryKeySelective(payInfo);
        }

        //TODO pay发送MQ消息，mall接受MQ消息
        /*
           为了方便直接串payInfo就可以，因为里面的数据足够多给mall使用。但是实际上再创建一个对象只弄mall需要的字段就好
           同时为了提高数据的安全性，不要直接传对象，转换成json格式后再传
           如果传的是对象，那么在rabbitmq可视化界面是看不见的。必须是字符才能显示
           MQ是给异步做的。所以不用担心接收时又从json转回对象，会造成性能上的损失
         */
        amqpTemplate.convertAndSend(QUEUE_PAY_NOTIFY,new Gson().toJson(payInfo));



        //4 支付成功后，告诉平台不要再重复发送异步通知了
        if(payResponse.getPayPlatformEnum()== BestPayPlatformEnum.WX){
            //告诉微信平台不要再通知了（查阅微信支付的支付结果通知来学习）
            return "<xml>\n" +
                    "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                    "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                    "</xml>";
        }else if (payResponse.getPayPlatformEnum()== BestPayPlatformEnum.ALIPAY){
            //告诉支付宝平台不要再通知了（查阅支付宝支付的支付结果通知来学习）
            return "success";
        }
        throw new RuntimeException("异步通知中错误的支付平台");
    }

    /**
     * @param orderId
     * @return
     */
    @Override
    public PayInfo queryByOrderId(String orderId) {
        PayInfo payInfo = payInfoMapper.selectByOrderNo(Long.parseLong(orderId));
        return payInfo;
    }
}
