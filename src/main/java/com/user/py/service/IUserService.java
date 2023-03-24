package com.user.py.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.user.py.mode.entity.User;
import com.user.py.mode.entity.vo.UserAvatarVo;
import com.user.py.mode.entity.vo.UserVo;
import com.user.py.mode.request.*;
import com.user.py.mode.resp.SafetyUserResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户 服务类
 * </p>
 *
 * @author ice
 * @since 2022-06-14
 */
public interface IUserService extends IService<User> {



    /**
     * 用户注册
     *
     * @param userRegisterRequest   账户
     * @return 返回id值
     */
    void userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userAccount 账户
     * @param password    密码
     * @return 用户信息
     */
    UserVo userLogin(String userAccount, String password ,String code,String uuid, HttpServletRequest request);


    /**
     *  判断是否是管理员
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     *  用户注销
     * @param request
     */
    void userLogout(HttpServletRequest request);

    /**
     * 修改用户
     * @param user
     * @return
     */
    Integer updateUser(User user,HttpServletRequest request);

    /**
     * ===============================================================
     * 根据标签搜索用户
     * @return
     */
    List<UserVo> searchUserTag(UserSearchTagAndTxtRequest userSearchTagAndTxtRequest);

    /**
     *  修改用户
     * @param updateUser 要修改的值
     * @param request
     * @return 4
     */
    UserVo getUserByUpdateID( UpdateUserRequest updateUser,HttpServletRequest request);

    Map<String,Object> friendUserName(User user, UserSearchPage userSearchPage);

    Map<String, Object> selectPageIndexList(long current, long size);


    /**
     * 根据单个标签搜索用户
     * @param tag
     * @param request
     * @return
     */
    List<UserVo> searchUserTag(String tag, HttpServletRequest request);

    /**
     * 匹配
     * @param num 数量
     * @param request 登录
     * @return 数组
     */
    List<UserVo> matchUsers(long num, HttpServletRequest request);

    /**
     * 用户忘记密码
     * @param registerRequest
     * @return
     */
    boolean userForget(UserRegisterRequest registerRequest);

    boolean seeUserEmail(String email);
    // 根据邮箱查找用户
    User forgetUserEmail(String email);

    List<UserAvatarVo>  getUserAvatarVoByIds(List list);

    SafetyUserResponse getCurrent(HttpServletRequest request);


    UserVo getUserVoByNameOrId(IdNameRequest idNameRequest, User loginUser);
}
