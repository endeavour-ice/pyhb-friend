package com.user.py.config;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @Author ice
 * @Date 2023/3/14 14:56
 * @Description: TODO
 */
@Component
public class ApplicationRun implements ApplicationRunner {

    /**
     * 项目初始化事件
     *
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        //这里省略了一些不必要的配置，可参考文档的说明

        Config config = new Config();
        config.protocol = "https";
        //支付宝网关
        config.gatewayHost = "openapi.alipaydev.com";
        config.signType = "RSA2";

        //应用id
        config.appId = "2021000122626627";

        // 为避免私钥随源码泄露，推荐从文件中读取私钥字符串而不是写入源码中
        //私钥
        config.merchantPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCq/6qZCdTVGrlAo6UjLBtrKlyBmNsncYY5oea/wchLdlFPKiWwgkGb5OXiaDRzBjyGZnLT7fEJuQAZXChLNDuvTSTGqmerppq5qnlcFCHvsxbYsdIvRF/aLGfJlC7n7SAtK3ApnYtXkRaXDjUu82V8Njtd1qZ+kIndgngLeZftlFnUhadf1WWHWf9HKLryRwuWBoAp5pNHQGgaN289ZAauvYph7y297n8HaKGv13phI1I7v2zH5LVvJu+c3WwOS/MBYFbVfBb4evo3x+cEj8mR83Jb9hmN9ZqGrqNWV6adcEmWnxhpSyzGOQxS70X7KDndzLcMGXOVIdCL+MVIGVgpAgMBAAECggEBAIOmQpmAUDOhV27Ejqb9lj5zZrUAEfnUFRrZ4uVPM/2lIgRo1ouGPSzglXwx7e+Apahzx4QSdLifjcv4f60mkaztH5s0JoyowAahkL2rYlVA9B3xCXkKkyFNfrpbM6GrllIwVzIy8MyCD3pMnfV/vmpa9mnQkG5wqTO49uMewjFN9zhpbyAy5CMxBd9GhWAT27nhhWAcdWj9Z3cmdMt3sdljyXAElUFoVUt+iTip2IAx7D3NMaQeuNni1WcJg5PFgS3X8OjYWDkGo09dHcadF+Uh0PN3BmlFRDloGsRXLnOeOHO+AU31qBZcHI/X1AiJzNVXPDMDKd0k5O5+UyK7nmECgYEA9oYZASGcVIqedNZdOj3j5hqbixMe5BdPPxNmQEoPlPcHR9DRhL/GdxvPLG9lPD30SJ19QdKmbiBz2z8in19NU4ClbksJCIMgF3ZJe1SQaXPAsNbmK+lFDGedHiVLK/BL59nJCq+UqyUIsRFORAkKNW5ilHwaQsbZrZSlS3Qx6zMCgYEAsZJeUtKb69EShPWfhTeboM4xxSTlXjk5CuLIrhvQ0zV4oV2JRw9MKZcnNt6HLmDTz0rCB1A+amoemwieSs9W1xgLFF+ztezRD7LrgAEKdsvfmNzRSecJDb4T0A6oxsSesuhGa9VHVzvGMwMEZHSVfIt7ayAs1FcykqbxE/EijzMCgYBebWkAfO5Tl7EwUbfuCizUQ9bWg6Fufts07upYr8WeKq0YPg0zCv4rO3S0sYqWc6ixc9m5r8VeI4IdNea+IxQMaxdy0r0IDKnrMrzcx7x1GAhz9CzAHx76rGmLVX+gue9pRIt0U2Lf11Sn9lgDSW8mnjyWYZ7xexeAHJLf/wbr7wKBgH6mTb421tzH3rSUV7V1Zvu3dUoOOcYNfEDmZYnA7x1leudkxl+3anQTifWZm8MZbpjf0inTMuFFVRTwfa4P2flJ/cUHSS8NfEL9Mvi2RVdbq9vkSyWxsgRqOMlhW0GBxUsHXgrVMK/oQ0Ho+f4IJQKSnXFXnoHIu5pblYDKsQJ/AoGAeF7Y3QBQ+bfPDZlOZMsVQJhLj3sf5liE2NrIqUcMj94tYF8DCpl95xc0DPhVGMPHugp8V+Z4cu+3XYr1FchaTJwZOPxrPxhU9D6F5IZaNwF1I/b4uaWNIlj1hOuXTaQc4ytdKLmcAtb02PeXBB6a1IbgAMp6xi+xBLS/AOI1GzE=";

        //注：如果采用非证书模式，则无需赋值上面的三个证书路径，改为赋值如下的支付宝公钥字符串即可
        //公钥
        config.alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvoirGzyb6oCZbyYAdxxgz4RgX5oDn0OXL+Y2p2DJykPr1sfaWuaV5qirz1rK8yjeq8Sc8io+qmZdj2UYOqqaExriVPo9AqHM3NZaEtOP2bRM/ZVWjn36QRSaFEMonYv/AIWQtA8QVjT9r6UuOyU7bzSS8HGsGoHyxdazCWCD3KSIQRBi1OfTxVNgXtxTqEmbeT7uvQ79gSdY0j8MMNNje113Ph0keoaSHKRgoERdhYzNHKuwR9s2q17Nuo1ophspKrqxv0QYNsb7oS20Rl0XMV4ZIWNpiTp9FhbAWZN//pF6WtK/+BruvbIB7tbkBiuyVVXc3+b4uLC9kMq/IX2CPwIDAQAB";
        config.notifyUrl = "http://h5g56s.natappfree.cc/py/oss/notify";
        //可设置异步通知接收服务地址（可选）
        //支付成功后的接口回调地址，不是回调的友好页面，不要弄混了

        //初始化支付宝SDK，使用Factory，设置客户端参数，只需设置一次，即可反复使用各种场景下的API Client
        Factory.setOptions(config);
        System.out.println("**********支付宝SDK初始化完成**********");
    }


}
