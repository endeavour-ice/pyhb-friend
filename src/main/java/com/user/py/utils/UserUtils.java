package com.user.py.utils;

import com.user.py.mode.domain.User;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mode.domain.vo.UserVo;
import com.user.py.mode.resp.SafetyUserResponse;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

import static com.user.py.mode.constant.UserConstant.ADMIN_ROLE;
import static com.user.py.mode.constant.UserConstant.USER_LOGIN_STATE;

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
        String token = (String) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (!StringUtils.hasText(token)) {
            throw new GlobalException(ErrorCode.NO_LOGIN);
        }
        User user = JwtUtils.getMemberIdByJwtToken(request);
        if (user == null) {
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
