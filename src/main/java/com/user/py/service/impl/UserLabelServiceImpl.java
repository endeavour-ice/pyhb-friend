package com.user.py.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mapper.UserLabelMapper;
import com.user.py.mode.entity.UserLabel;
import com.user.py.mode.request.UserLabelRequest;
import com.user.py.mode.resp.UserLabelResponse;
import com.user.py.service.IUserLabelService;
import com.user.py.utils.UserUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 标签表 服务实现类
 * </p>
 *
 * @author ice
 * @since 2022-09-16
 */
@Service
public class UserLabelServiceImpl extends ServiceImpl<UserLabelMapper, UserLabel> implements IUserLabelService {

    @Override
    public boolean addUserLabel(UserLabelRequest labelRequest, HttpServletRequest request) {
        boolean isAdmin = UserUtils.isAdmin(request);
        if (!isAdmin) {
            throw new GlobalException(ErrorCode.NO_AUTH);
        }
        if (labelRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        String label = labelRequest.getLabel();
        String labelType = labelRequest.getLabelType();
        if (!StringUtils.hasText(label) || !StringUtils.hasText(labelType)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        UserLabel userLabel = new UserLabel();
        userLabel.setLabel(label);
        userLabel.setLabelType(labelType);
        int insert = baseMapper.insert(userLabel);
        return insert>0;
    }

    /**
     * 获取所有的标签
     * @param request
     * @return
     */
    @Override
    public Map<String,String> getLabel(HttpServletRequest request) {
        UserUtils.getLoginUser(request);
        List<UserLabel> list = this.list();
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        if (list.isEmpty()) {
            return null;
        }
        return list.stream().collect(Collectors.toMap(UserLabel::getId, UserLabel::getLabel));
    }

    @Override
    public List<UserLabelResponse> getUserLabel(HttpServletRequest request) {
        UserUtils.getLoginUser(request);
        List<UserLabel> list = this.list();

        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        Map<String, List<UserLabel>> listMap =
                list.stream().collect(Collectors.groupingBy(UserLabel::getLabelType));
        List<UserLabelResponse> responses = new ArrayList<>();
        for (String s : listMap.keySet()) {
            List<UserLabel> labelList = listMap.get(s);
            List<String> stringList = labelList.stream().map(UserLabel::getLabel).collect(Collectors.toList());
            UserLabelResponse response = new UserLabelResponse();
            response.setLabelType(s);
            response.setLabel(stringList);
            responses.add(response);
        }

        return responses;
    }

    @Override
    public boolean delUserLabel(String id, HttpServletRequest request) {
        UserUtils.getLoginUser(request);
        if (!StringUtils.hasText(id)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        UserLabel label = this.getById(id);
        if (label == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR,"没有该数据");
        }

        return this.removeById(label);
    }
}
