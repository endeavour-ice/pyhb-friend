package com.user.py.controller.OssController;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.user.py.annotation.AuthSecurity;
import com.user.py.annotation.CurrentLimiting;
import com.user.py.common.B;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mode.entity.User;
import com.user.py.mode.enums.UserRole;
import com.user.py.service.OssService;
import com.user.py.utils.ResponseEmail;
import com.user.py.utils.UserUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


/**
 * @author ice
 * @date 2022/9/17 12:46
 */
@RestController
@RequestMapping("/oss")
public class OssController {

    @Resource
    private OssService ossService;


    @GetMapping("/pay/{subject}/{traceNo}/{totalAmount}") // &subject=xxx&traceNo=xxx&totalAmount=xxx
    public String pay(@PathVariable("subject") String subject,@PathVariable("traceNo") String traceNo,@PathVariable("totalAmount") String totalAmount) {
        // 1. 设置参数（全局只需设置一次）
        AlipayTradePagePayResponse response;
        try {
            // 2. 发起API调用（以创建当面付收款二维码为例）
            response = Factory.Payment.Page().pay(subject, traceNo, totalAmount, "");
            // 3. 处理响应或异常

        } catch (Exception e) {
            System.err.println("调用遭遇异常，原因：" + e.getMessage());
            throw new GlobalException(ErrorCode.NULL_ERROR,"支付宝测试接口,出错正常");
        }
        return response.getBody();
    }
    @PostMapping("/notify")
    public String notifyS(HttpServletRequest request) {

        HashMap<String, String> paramMap = new HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (String s : parameterMap.keySet()) {
            paramMap.put(s, request.getParameter(s));
        }
        try {
            if (Factory.Payment.Common().verifyNotify(paramMap)) {
                System.out.println("名称"+paramMap.get("subject"));
                System.out.println("交易状态" +paramMap.get("trade_status"));
                System.out.println("凭证"+paramMap.get("trade_no"));
                System.out.println("订单号"+paramMap.get("out_trade_no"));
                System.out.println("金额"+paramMap.get("total_amount"));
                System.out.println("id"+paramMap.get("buyer_id"));
                System.out.println("付款时间"+paramMap.get("gmt_create"));
                System.out.println("付款金额"+paramMap.get("buyer_pay_amount"));
            }
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.NULL_ERROR,"支付宝测试接口,出错正常");
        }
        System.out.println(paramMap);
        return paramMap.toString();
    }
    /**
     * 用户头像上传
     * @param file
     * @param request
     * @return
     */
    @PostMapping("/file/upload")
    @CurrentLimiting
    @AuthSecurity(isNoRole = {UserRole.TEST})
    public B<String> upFile(MultipartFile file, HttpServletRequest request) {

        String url = ossService.upload(file, request);
        return B.ok(url);
    }

    /**
     * 队伍头像上传
     * @param file
     * @param request
     * @param teamID
     * @return
     */
    @PostMapping("/file/upload/team/{teamID}")
    @CurrentLimiting
    @AuthSecurity(isNoRole = {UserRole.TEST})
    public B<String> upFileByTeam(MultipartFile file, HttpServletRequest request, @PathVariable String teamID) {
        User loginUser = UserUtils.getLoginUser(request);
        String url = ossService.upFileByTeam(file, loginUser, teamID);
        return B.ok(url);
    }

    /**
     * 注册邮箱验证
     *
     * @param email
     * @return
     */
    @PostMapping("/send")
    @CurrentLimiting
    public B<Boolean> sendEMail(@RequestBody ResponseEmail email, HttpServletRequest request) {
        boolean is = ossService.sendRegisterEMail(email, request);
        return B.ok(is);
    }

    /**
     * 忘记密码邮箱验证
     *
     * @param email
     * @return
     */
    @PostMapping("/sendForget")
    @CurrentLimiting
    public B<Boolean> sendForgetEMail(@RequestBody ResponseEmail email, HttpServletRequest request) {
        boolean is = ossService.sendForgetEMail(email, request);
        return B.ok(is);
    }

    /**
     * 发送绑定邮件的验证码
     *
     * @param email   邮件
     * @param request
     * @return
     */
    @PostMapping("/sendBinDing")
    @CurrentLimiting
    public B<Boolean> sendBinDingEMail(@RequestBody ResponseEmail email, HttpServletRequest request) {
        boolean is = ossService.sendBinDingEMail(email, request);
        return B.ok(is);
    }
}
