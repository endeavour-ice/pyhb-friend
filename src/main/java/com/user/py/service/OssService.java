package com.user.py.service;

import com.user.py.mode.entity.User;
import com.user.py.utils.ResponseEmail;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author ice
 * @date 2022/9/17 12:48
 */

public interface OssService {
    String upload(MultipartFile file,HttpServletRequest request);

    String upFileByTeam(MultipartFile file, User loginUser, String teamID);

    boolean sendRegisterEMail(ResponseEmail email, HttpServletRequest request);

    boolean sendForgetEMail(ResponseEmail email, HttpServletRequest request);

    boolean sendBinDingEMail(ResponseEmail email, HttpServletRequest request);
}
