package com.user.py.controller.UserController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.user.py.common.B;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mode.entity.UserNotice;
import com.user.py.service.IUserNoticeService;
import com.user.py.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 公告表 前端控制器
 * </p>
 *
 * @author ice
 * @since 2022-09-18
 */
@RestController
@RequestMapping("/api/userNotice")
public class UserNoticeController {
    @Autowired
    private IUserNoticeService noticeService;

    @GetMapping("/getNotice")
    public B<String> getNotice(HttpServletRequest request, @RequestParam("region")Integer region) {
        UserUtils.getLoginUser(request);
        QueryWrapper<UserNotice> wrapper = new QueryWrapper<>();
        wrapper.eq("region", region);
        UserNotice notice = noticeService.getOne(wrapper);
        if (notice == null) {
            return B.ok(null);
        }
        return B.ok(notice.getNotice());
    }

    @PostMapping("/addNotice")
    public B<Boolean> addNotice(HttpServletRequest request,UserNotice userNotice) {
        boolean admin = UserUtils.isAdmin(request);
        if (!admin) {
            throw new GlobalException(ErrorCode.NO_AUTH);
        }
        if (userNotice == null ) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        if ( userNotice.getRegion()==null &&userNotice.getRegion() <= 0 && !StringUtils.hasText(userNotice.getNotice())) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        if (!StringUtils.hasText(userNotice.getNotice())) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        boolean save = noticeService.save(userNotice);
        if (save) {
            return B.ok();
        }
        return B.error();
    }

    @PostMapping ("/updateNotice")
    public B<Boolean> updateNotice(HttpServletRequest request,UserNotice userNotice) {
        boolean admin = UserUtils.isAdmin(request);
        if (!admin) {
            throw new GlobalException(ErrorCode.NO_AUTH);
        }
        boolean save = noticeService.updateById(userNotice);
        if (save) {
            return B.ok();
        }
        return B.error();
    }

}
