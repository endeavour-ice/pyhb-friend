package com.user.py.controller.PostController;

import com.user.py.common.B;
import com.user.py.utils.IpUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 帖子收藏记录 前端控制器
 * </p>
 *
 * @author ice
 * @since 2023-03-10
 */
@RestController
@RequestMapping("/post_collect/postCollect")
public class PostCollectController {
    @GetMapping("/ip")
    public B<List<String>> getIp(HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        String ipAddress = IpUtils.getIpAddress(request);

        return B.ok(Arrays.asList(ip, ipAddress));
    }
}
