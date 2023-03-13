package com.user.py.controller.UserController;


import com.user.py.common.B;
import com.user.py.mode.request.UserLabelRequest;
import com.user.py.mode.resp.UserLabelResponse;
import com.user.py.service.IUserLabelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标签表 前端控制器
 * </p>
 *
 * @author ice
 * @since 2022-09-16
 */
@RestController
@RequestMapping("/api/userLabel")
public class UserLabelController {

    @Resource
    private IUserLabelService labelService;



    @PostMapping("/addUserLabel")
    public B<Boolean> addUserLabel(UserLabelRequest labelRequest, HttpServletRequest request) {
        boolean is= labelService.addUserLabel(labelRequest,request);
        if (!is) {
            return B.error();
        }
        return B.ok();
    }

    /**
     * 获取所有的标签
     * @param request
     * @return
     */
    @GetMapping("/getLabel")
    public B<Map<String,String>> getLabel(HttpServletRequest request) {
        Map<String,String> list= labelService.getLabel(request);
        return B.ok(list);
    }

    @GetMapping("/getUserLabel")
    public B<List<UserLabelResponse>> getUserLabel(HttpServletRequest request) {
        List<UserLabelResponse> list= labelService.getUserLabel(request);
        return B.ok(list);
    }

    @GetMapping("/delUserLabel")
    public B<Boolean> delUserLabel(@RequestParam("id")String id, HttpServletRequest request) {
        boolean isDelete= labelService.delUserLabel(id,request);
        if (!isDelete) {
            return B.error();
        }
        return B.ok();
    }
}
