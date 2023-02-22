package com.user.py.mode.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author ice
 * @Date 2022/10/1 15:17
 * @PackageName:com.user.py.mode.request
 * @ClassName: UserSearchTagAndTxtRequest
 * @Description: TODO
 * @Version 1.0
 */
@Data
public class UserSearchTagAndTxtRequest implements Serializable {
    private static final long serialVersionUID = -6392423350096166358L;
    private List<String> tagNameList;
    private String searchTxt;
}
