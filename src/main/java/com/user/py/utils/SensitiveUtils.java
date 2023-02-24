package com.user.py.utils;

import toolgood.words.StringSearch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/2/24 12:15
 * @Description: TODO
 */
public class SensitiveUtils {
    private SensitiveUtils(){

    }
    private static final List<String> list = new ArrayList<>();
    /**
     * 过滤字符
     *
     * @param text
     * @return
     */
    public static String sensitive(String text) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("static/mg.txt");
        if (url == null) {
            return text;
        }
        String path = url.getPath();
        if (list.isEmpty()) {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
        }
        StringSearch search = new StringSearch();
        search.SetKeywords(list);
        return search.Replace(text);
    }
}
