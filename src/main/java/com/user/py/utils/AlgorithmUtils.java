package com.user.py.utils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 算法工具类
 *
 * @author ice
 * @date 2022/9/19 17:08
 */
public class AlgorithmUtils {

    /**
     * 编辑距离
     * 地址 =>  https://blog.csdn.net/DBC_121/article/details/104198838
     *
     * @param word1 1
     * @param word2 2
     * @return 返回的 最小  编辑距离
     */
    public static int minDistance(List<String> word1, List<String> word2) {
        int n = word1.size();
        int m = word2.size();

        if (n * m == 0) {
            return n + m;
        }

        int[][] d = new int[n + 1][m + 1];
        for (int i = 0; i < n + 1; i++) {
            d[i][0] = i;
        }

        for (int j = 0; j < m + 1; j++) {
            d[0][j] = j;
        }

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < m + 1; j++) {
                int left = d[i - 1][j] + 1;
                int down = d[i][j - 1] + 1;
                int left_down = d[i - 1][j - 1];
                if (!word1.get(i - 1).equals(word2.get(j - 1))) {
                    left_down += 1;
                }
                d[i][j] = Math.min(left, Math.min(down, left_down));
            }
        }
        return d[n][m];
    }


    private static final Map<String, int[]> vectorMap = new HashMap<>();

    public static double getSimilarity(@NotNull List<String> string1, List<String> string2) {

        int[] tempArray;

        for (String character1 : string1) {
            if (vectorMap.containsKey(character1)) {
                vectorMap.get(character1)[0]++;
            } else {
                tempArray = new int[2];
                tempArray[0] = 1;
                vectorMap.put(character1, tempArray);
            }
        }
        for (String character2 : string2) {
            if (vectorMap.containsKey(character2)) {
                vectorMap.get(character2)[1]++;
            } else {
                tempArray = new int[2];
                tempArray[1] = 1;
                vectorMap.put(character2, tempArray);
            }
        }
        double result;
        result = pointMulti() / sqrtMulti();
        return result;
    }


    private static double sqrtMulti() {
        double result = 0;
        result = squares();
        result = Math.sqrt(result);
        return result;
    }

    // 求平方和
    private static double squares() {
        double result1 = 0;
        double result2 = 0;
        Set<String> keySet = AlgorithmUtils.vectorMap.keySet();
        for (String character : keySet) {
            int[] temp = AlgorithmUtils.vectorMap.get(character);
            result1 += (temp[0] * temp[0]);
            result2 += (temp[1] * temp[1]);
        }
        return result1 * result2;
    }

    // 点乘法
    private static double pointMulti() {
        double result = 0;
        Set<String> keySet = AlgorithmUtils.vectorMap.keySet();
        for (String character : keySet) {
            int[] temp = AlgorithmUtils.vectorMap.get(character);
            result += (temp[0] * temp[1]);
        }
        return result;
    }


}
