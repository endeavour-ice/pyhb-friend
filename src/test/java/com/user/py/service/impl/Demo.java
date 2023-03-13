package com.user.py.service.impl;

import java.util.Locale;

/**
 * @Author ice
 * @Date 2023/3/9 10:48
 * @Description: TODO
 */
public class Demo {
    public static void main(String[] args) {
        String hello = "Hello World";
        System.out.println(UpperCase(hello));
        System.out.println(replace(hello));
        System.out.println(mi(6, 7, 8));
    }
    public static String UpperCase(String s) {
        return s.toUpperCase(Locale.ROOT);
    }
    public static String replace(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char charAt = s.charAt(i);
            if (charAt == 'o') {
                charAt = '*';
            }
            stringBuilder.append(charAt);
        }
        return stringBuilder.toString();
    }
    public static void getDay(int year,int b,int c) {
        int t;
        t = b * 30;
        t += c;
        System.out.println(t);
    }
    public static int mi(int a, int b, int c) {
        int z;
        int d;
        z = Math.max(a, b);
        d = Math.max(c, z);
        for (int i = d; ; i++) {
            if (i % a == 0 && i % b == 0 && i % c == 0) {
                return i;
            }
        }
    }
}
