package com.user.py.once;


import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.extern.log4j.Log4j2;

/**
 * @author ice
 * @date 2022/7/13 12:22
 */

// 有个很重要的点 DemoDataListener 不能被spring管理，要每次读取excl都要new,然后里面用到spring可以构造方法传进去
@Log4j2
public class UserListener implements ReadListener<DemoUser> {


    /**
     * 这个每一条数据解析都会来调用
     *
     * @param data    one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param context
     */
    @Override
    public void invoke(DemoUser data, AnalysisContext context) {
        System.out.println(data);
    }

    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        System.out.println("解析完成");
    }


}
