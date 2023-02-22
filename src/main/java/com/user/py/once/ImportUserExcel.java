package com.user.py.once;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author ice
 * @date 2022/7/13 12:13
 * 导入数据
 */

public class ImportUserExcel extends UserListener {
    public static void simpleRead() {
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
        String fileName = "C:\\Users\\BING\\Desktop\\test.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        // 这里每次会读取100条数据 然后返回过来 直接调用使用数据就行
        List<DemoUser> userList = EasyExcel.read(fileName, DemoUser.class, new UserListener()).sheet().doReadSync();
        System.out.println("总数" + userList.size());
        Map<String, List<DemoUser>> collect = userList.parallelStream().filter(demoUser -> StringUtils.isNotEmpty(demoUser.getUsername())).collect(Collectors.groupingBy(DemoUser::getUsername));
        System.out.println("不重复的昵称数" + collect.keySet().size());
    }

    public static void Write() {
        // 写法1 JDK8+
        // since: 3.0.0-beta1
        String fileName = "C:\\Users\\BING\\Desktop\\测试.xlsx";
        // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
        // 如果这里想使用03 则 传入excelType参数即可

        List<DemoUser> users = new ArrayList<>();
        for (int i = 0; i < 100000000; i++) {
            DemoUser demoUser = new DemoUser();
            demoUser.setUsername("asd" + i);
            demoUser.setPlanetCode("saddas" + i);
            users.add(demoUser);
        }
        // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
        EasyExcel.write(fileName, DemoUser.class).sheet("模板").doWrite(users);
    }

    public static void main(String[] args) {
        TreeMap<Object, Object> objectObjectTreeMap = new TreeMap<>();
        objectObjectTreeMap.put("sad", "asd");
        System.out.println(objectObjectTreeMap);

    }
}
