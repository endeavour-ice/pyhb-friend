package com.user.py.mode.request;

import com.user.py.mode.dto.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author ice
 * @Date 2023/3/5 10:34
 * @Description: TODO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserSearchPage extends PageRequest {
    private static final long serialVersionUID = 32927742934291839L;
    private String userName;
}
