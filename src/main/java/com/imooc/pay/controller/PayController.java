package com.imooc.pay.controller;

import com.imooc.pay.dao.PayInfoMapper;
import com.imooc.pay.impl.PayService;
import com.imooc.pay.pojo.PayInfo;
import com.imooc.pay.service.IPayService;
import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/pay")
@Slf4j
public class PayController {

    @Autowired//再进一步
    private PayService payService;
    @Autowired
    private WxPayConfig wxPayConfig;

    @GetMapping("/create")
    /**
     *要呈现一个网页，要在网页里写js代码，需要用到一个模板技术，把它渲染出来。在pom.xml添加依赖freemarker
     *返回类型是ModelAndViews是一个网页
     * 为不把订单号和金额参数写死，需要把其当作参数传进函数。当按照这种方式时，在打开浏览器后需要在网址后加入?orderId=订单号&amount=金额
     *
     */
    //在下面的create函数加@RequestParam是可以直接在网页把参数传入
    public ModelAndView create(@RequestParam("orderId") String orderId, @RequestParam("amount") BigDecimal amount, @RequestParam("payType") BestPayTypeEnum bestPayTypeEnum){

        PayResponse response = payService.create(orderId, amount,bestPayTypeEnum);

        Map<String,String> map=new HashMap<>();
          /*
                //对网页进行渲染，如果不进行渲染，那生成的二维码字符串是要在ftl文件写死的
                //支付方式不同渲染就不同
                微信的NATIVE支付方式   WXPAY_NATIVE用code_url
                支付宝的PC支付方式     ALIPAY_PC用body
          */
          if(bestPayTypeEnum==BestPayTypeEnum.WXPAY_NATIVE){
              map.put("codeUrl",response.getCodeUrl());//把code_url的参数放进去
              map.put("orderId",orderId);
              map.put("returnUrl",wxPayConfig.getReturnUrl());//returnUrl在BestPayConfig配置文件里
              /**
               * ModelAndView(viewName)里面的参数是视图的名字
               * freemarker模板默认的是ftl，所以在templates文件下创建一个create.ftl文件
               * 由于渲染，需要在ModelAndView传入map
               * 之后需要在视图的text接收。之前是code_url的内容，现在是按照固定的格式来接受。具体格式可以在freemarker语法里学习到
               */
              return new ModelAndView("createForWXNATIVE",map);//里面的参数是视图的名字,只需写文件名不用加.ftl后缀
          }else if (bestPayTypeEnum==BestPayTypeEnum.ALIPAY_PC){
              map.put("body",response.getBody());
              return new ModelAndView("createForALIPAYPC",map);//里面的参数是视图的名字,只需写文件名不用加.ftl后缀
          }
        throw new RuntimeException("暂不支持的支付类型");//在这写了抛出异常之后，在PayService就不用写了

    }


    @PostMapping("/notify")
    @ResponseBody
    /**
     * 该方法时用来接收post请求的
     * notify是关键字，又因为是异步通知，所以起名asyncNotify
     * 签名是要根据密钥来产生的，不能伪造。所以密钥要保护好。其他数据可能被伪造
     * 函数参数不止一个，所以用RequestBody来接受参数
     * 只想让微信通知一次，见PayService内容。但可能因为网页格式的问题，需要在该函数加注释@ResponseBody
     */
    public String asyncotify(@RequestBody String notifyData){
        return payService.asyncNotify(notifyData);
        //log.info("notifyData={}",notifyData);//之前的步骤
    }

    @GetMapping("/queryByOrderId")
    @ResponseBody
    public PayInfo queryByOrderId(@RequestParam String orderId){
      // log.info("查询支付记录....");
        return payService.queryByOrderId(orderId);
    }
}
