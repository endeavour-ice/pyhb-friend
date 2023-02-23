package com.user.py.mode.constant;

/**
 * @Author ice
 * @Date 2023/2/23 10:45
 * @Description: Post状态
 */
public class PostStatus {
    /**
     * 待审核
     */
    Integer WAIT_EXAMINE  = 0;
    /**
     * 审核中
     */
    Integer EXAMINE = 1;
    /**
     * 成功
     */
    Integer EXAMINE_SUCCESS = 2;
    /**
     * 失败
     */
    Integer EXAMINE_ERROR = 3;
}
