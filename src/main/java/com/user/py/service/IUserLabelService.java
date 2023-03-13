package com.user.py.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.user.py.mode.entity.UserLabel;
import com.user.py.mode.request.UserLabelRequest;
import com.user.py.mode.resp.UserLabelResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标签表 服务类
 * </p>
 *
 * @author ice
 * @since 2022-09-16
 */
public interface IUserLabelService extends IService<UserLabel> {

    boolean addUserLabel(UserLabelRequest labelRequest, HttpServletRequest request);

    Map<String,String> getLabel(HttpServletRequest request);

    List<UserLabelResponse> getUserLabel(HttpServletRequest request);

    boolean delUserLabel(String id, HttpServletRequest request);
}
