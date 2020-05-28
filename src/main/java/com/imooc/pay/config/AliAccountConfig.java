package com.imooc.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix ="ali")
@Data
public class AliAccountConfig {
    private String appid;
    private String privateKey;
    private String apaliPayPublicKeypid;
    private String notifyUrl;
    private String returnUrl;


}
