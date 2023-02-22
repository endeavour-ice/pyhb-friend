package com.user.py.controller.OssController;

import com.user.py.annotation.CurrentLimiting;
import com.user.py.common.B;
import com.user.py.mode.domain.User;
import com.user.py.service.OssService;
import com.user.py.utils.ResponseEmail;
import com.user.py.utils.UserUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


/**
 * @author ice
 * @date 2022/9/17 12:46
 */
@RestController
@RequestMapping("/oss")
public class OssController {

    @Resource
    private OssService ossService;


    @PostMapping("/file/upload")
    @CurrentLimiting
    public B<String> upFile(MultipartFile file, HttpServletRequest request) {
        User loginUser = UserUtils.getLoginUser(request);
        String url = ossService.upload(file, loginUser);
        return B.ok(url);
    }

    @PostMapping("/file/upload/team/{teamID}")
    @CurrentLimiting
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
