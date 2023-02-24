package com.user.py.service;

import java.util.LinkedHashMap;

/**
 * @Author ice
 * @Date 2023/2/24 14:01
 * @Description: TODO
 */
public class Demo {
    public static void main(String[] args) {
        A a = new A();
        a = null;
        LinkedHashMap<String, String> stringStringLinkedHashMap = new LinkedHashMap<>();
        stringStringLinkedHashMap.put("sad", "Sd");
        stringStringLinkedHashMap.put("asd", "asd");
        stringStringLinkedHashMap.put("ssdfad", "asd");
        new Thread(() -> {
            while (true) {

            }
        }).start();
        System.out.println(stringStringLinkedHashMap);
        System.gc();
    }
}

class A {
    String string;
    @Override
    protected void finalize() throws Throwable {
        System.out.println("对象被回收");
    }
}
