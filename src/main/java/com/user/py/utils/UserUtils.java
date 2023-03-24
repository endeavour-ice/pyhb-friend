package com.user.py.utils;

import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mode.entity.User;
import com.user.py.mode.entity.vo.UserVo;
import com.user.py.mode.enums.UserStatus;
import com.user.py.mode.resp.SafetyUserResponse;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

import static com.user.py.mode.constant.UserConstant.ADMIN_ROLE;

/**
 * @author ice
 * @date 2022/8/23 11:49
 */

public class UserUtils {
    /**
     * 通过解析获取用户信息
     * @param request 请求
     * @return 用户信息
     */
    public  static User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            throw new GlobalException(ErrorCode.NO_LOGIN);
        }

        User user = JwtUtils.getMemberIdByJwtToken(request);
        if (user == null|| !StringUtils.hasText(user.getId())) {
            throw new GlobalException(ErrorCode.NO_LOGIN);
        }
        return user;
    }
    public static UserVo getSafetyUser(User user) {
        if (user == null) {
            return null;
        }
        UserVo cleanUser = new UserVo();
        cleanUser.setId(user.getId());
        cleanUser.setUsername(user.getUsername());
        cleanUser.setUserAccount(user.getUserAccount());
        cleanUser.setAvatarUrl(user.getAvatarUrl());
        cleanUser.setGender(user.getGender());
        cleanUser.setTel(user.getTel());
        cleanUser.setEmail(user.getEmail());
        cleanUser.setUserStatus(user.getUserStatus());
        cleanUser.setCreateTime(user.getCreateTime());
        cleanUser.setRole(user.getRole());
        cleanUser.setPlanetCode(user.getPlanetCode());
        cleanUser.setTags(user.getTags());
        cleanUser.setProfile(user.getProfile());
        return cleanUser;
    }
    public static SafetyUserResponse getSafetyUserResponse(User user) {
        if (user == null) {
            return null;
        }
        SafetyUserResponse safetyUserResponse = new SafetyUserResponse();
        safetyUserResponse.setId(user.getId());
        safetyUserResponse.setUsername(user.getUsername());
        safetyUserResponse.setUserAccount(user.getUserAccount());
        safetyUserResponse.setAvatarUrl(user.getAvatarUrl());
        safetyUserResponse.setGender(user.getGender());
        safetyUserResponse.setTags(user.getTags());
        safetyUserResponse.setProfile(user.getProfile());
        safetyUserResponse.setTel(user.getTel());
        Integer status = user.getUserStatus();
        if ( status== UserStatus.NORMAL.getKey()) {
            safetyUserResponse.setStatus("公开");
        } else if (status == UserStatus.PRIVATE.getKey()) {
            safetyUserResponse.setStatus("私密");
        }
        safetyUserResponse.setEmail(user.getEmail());
        return safetyUserResponse;
    }
    public static boolean isAdmin(HttpServletRequest request) {
        // 仅管理员查询
        User user = getLoginUser(request);
        return Objects.equals(user.getRole(), ADMIN_ROLE);
    }
    public static boolean isAdmin(User user) {
        // 仅管理员查询
        return Objects.equals(user.getRole(), ADMIN_ROLE);
    }
}
