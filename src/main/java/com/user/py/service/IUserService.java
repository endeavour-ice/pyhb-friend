package com.user.py.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.user.py.mode.domain.User;
import com.user.py.mode.domain.vo.UserAvatarVo;
import com.user.py.mode.request.UpdateUserRequest;
import com.user.py.mode.request.UserRegisterRequest;
import com.user.py.mode.request.UserSearchTagAndTxtRequest;

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
    String userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userAccount 账户
     * @param password    密码
     * @return 用户信息
     */
    User userLogin(String userAccount, String password , HttpServletRequest request);


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
    List<User> searchUserTag(UserSearchTagAndTxtRequest userSearchTagAndTxtRequest);

    /**
     *  修改用户
     * @param updateUser 要修改的值
     * @param request
     * @return 4
     */
    int getUserByUpdateID( UpdateUserRequest updateUser,HttpServletRequest request);

    List<User> friendUserName(String userID, String friendUserName);

    Map<String, Object> selectPageIndexList(long current, long size);

    Map<String, Object> searchUser(HttpServletRequest request, String username, Long current, Long size);

    /**
     * 根据单个标签搜索用户
     * @param tag
     * @param request
     * @return
     */
    List<User> searchUserTag(String tag, HttpServletRequest request);

    /**
     * 匹配
     * @param num 数量
     * @param request 登录
     * @return 数组
     */
    List<User> matchUsers(long num, HttpServletRequest request);

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
}
