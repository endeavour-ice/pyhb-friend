package com.user.py.utils;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 算法工具类
 *
 * @author ice
 * @date 2022/9/19 17:08
 */
@Slf4j
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

    /**
     * 余弦相似度算法
     *
     * @param string1
     * @param string2
     * @return
     */
    public static double getSimilarity(String string1, String string2) {
        return CosineSimilarity.getSimilarity(string1, string2);
    }

    /**
     * 计算两个字符串的相识度
     */

    public static final String content1 = "今天小小和爸爸一起去摘草莓，小小说今天的草莓特别的酸，而且特别的小，关键价格还贵";
    public static final String content2 = "今天小小和妈妈一起去草原里采草莓，今天的草莓味道特别好，而且价格还挺实惠的";


    public static void main(String[] args) {
        double score = CosineSimilarity.getSimilarity(content1, content2);
        System.out.println("相似度：" + score);

    }


    /**
     * 中文分词工具类
     */
    public static class Tokenizer {
        /**
         * 分词
         */
        public static List<Word> segment(String sentence) {
            //1、 采用HanLP中文自然语言处理中标准分词进行分词
            List<Term> termList = HanLP.segment(sentence);
            // 上面控制台打印信息就是这里输出的
            //System.out.println(termList.toString());
            // 2、重新封装到Word对象中（term.word代表分词后的词语，term.nature代表改词的词性）
            return termList.stream().map(term -> new Word(term.word, term.nature.toString())).collect(Collectors.toList());
        }
    }


    /**
     * 封装分词结果
     */
    @Data
    private static class Word implements Comparable<Object> {
        // 词名
        private String name;
        // 词性
        private String pos;
        // 权重，用于词向量分析
        private Float weight;

        public Word(String name, String pos) {
            this.name = name;
            this.pos = pos;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.name);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Word other = (Word) obj;
            return Objects.equals(this.name, other.name);
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            if (name != null) {
                str.append(name);
            }
            if (pos != null) {
                str.append("/").append(pos);
            }
            return str.toString();
        }

        @Override
        public int compareTo(@NotNull Object o) {
            if (this == o) {
                return 0;
            }
            if (this.name == null) {
                return -1;
            }
            if (!(o instanceof Word)) {
                return 1;
            }
            String t = ((Word) o).getName();
            if (t == null) {
                return 1;
            }
            return this.name.compareTo(t);
        }
    }

    /**
     * 判定方式：余弦相似度，通过计算两个向量的夹角余弦值来评估他们的相似度 余弦夹角原理： 向量a=(x1,y1),向量b=(x2,y2) similarity=a.b/|a|*|b| a.b=x1x2+y1y2 * |a|=根号[(x1)^2+(y1)^2],|b|=根号[(x2)^2+(y2)^2]
     */
    private static class CosineSimilarity {

        /**
         * 1、计算两个字符串的相似度
         */
        public static double getSimilarity(String text1, String text2) {
            //如果wei空，或者字符长度为0，则代表完全相同
            if (StringUtils.isBlank(text1) && StringUtils.isBlank(text2)) {
                return 1.0;
            }
            //如果一个为0或者空，一个不为，那说明完全不相似
            if (StringUtils.isBlank(text1) || StringUtils.isBlank(text2)) {
                return 0.0;
            }
            //这个代表如果两个字符串相等那当然返回1了（这个我为了让它也分词计算一下，所以注释掉了）
            if (text1.equalsIgnoreCase(text2)) {
                return 1.0;
            }
            //第一步：进行分词
            List<Word> words1 = Tokenizer.segment(text1);
            List<Word> words2 = Tokenizer.segment(text2);
            return getSimilarity(words1, words2);
        }

        /**
         * 2、对于计算出的相似度保留小数点后六位
         */
        public static double getSimilarity(List<Word> words1, List<Word> words2) {
            double score = getSimilarityImpl(words1, words2);
            // (int) (score * 1000000 + 0.5) 其实代表保留小数点后六位, 因为1034234 .213 强制转换不就是1034234。对于强制转换添加0 .5就等于四舍五入
            score = (int) (score * 1000000 + 0.5) / (double) 1000000;
            return score;
        }

        /**
         * 文本相似度计算 判定方式：余弦相似度，通过计算两个向量的夹角余弦值来评估他们的相似度
         * 余弦夹角原理： 向量a=(x1,y1),向量b=(x2,y2) similarity=a.b/|a|*|b| a.b=x1x2+y1y2 * |a|=根号[(x1)^2+(y1)^2],|b|=根号[(x2)^2+(y2)^2]
         */
        public static double getSimilarityImpl(List<Word> words1, List<Word> words2) {
            // 向每一个Word对象的属性都注入weight（权重）属性值
            taggingWeightByFrequency(words1, words2);
            //第二步：计算词频
            // 通过上一步让每个Word对象都有权重值，那么在封装到map中（key是词，value是该词出现的次数（即权重））
            Map<String, Float> weightMap1 = getFastSearchMap(words1);
            Map<String, Float> weightMap2 = getFastSearchMap(words2);
            //将所有词都装入set容器中
            Set<Word> words = new HashSet<>();
            words.addAll(words1);
            words.addAll(words2);
            AtomicFloat ab = new AtomicFloat();
            // a.b
            AtomicFloat aa = new AtomicFloat();
            // |a|的平方
            AtomicFloat bb = new AtomicFloat();
            // |b|的平方    第三步：写出词频向量，后进行计算
            words.parallelStream().forEach(word -> {
                //看同一词在a、b两个集合出现的此次
                Float x1 = weightMap1.get(word.getName());
                Float x2 = weightMap2.get(word.getName());
                if (x1 != null && x2 != null) {
                    //x1x2
                    float oneOfTheDimension = x1 * x2;
                    //+
                    ab.addAndGet(oneOfTheDimension);
                }
                if (x1 != null) {
                    //(x1)^2
                    float oneOfTheDimension = x1 * x1;
                    //+
                    aa.addAndGet(oneOfTheDimension);
                }
                if (x2 != null) {
                    //(x2)^2
                    float oneOfTheDimension = x2 * x2;
                    //+
                    bb.addAndGet(oneOfTheDimension);
                }
            });
            //|a| 对aa开方
            double aaa = Math.sqrt(aa.doubleValue());
            //|b| 对bb开方
            double bbb = Math.sqrt(bb.doubleValue());
            //使用BigDecimal保证精确计算浮点数double aabb = aaa * bbb;
            BigDecimal aabb = BigDecimal.valueOf(aaa).multiply(BigDecimal.valueOf(bbb));
            //  similarity = a.b / | a |*|b |
            //divide参数说明：aabb被除数,9表示小数点后保留9位，最后一个表示用标准的四舍五入法
            return BigDecimal.valueOf(ab.get()).divide(aabb, 9, RoundingMode.HALF_UP).doubleValue();
        }

        /**
         * 向每一个Word对象的属性都注入weight（权重）属性值
         */
        protected static void taggingWeightByFrequency(List<Word> words1, List<Word> words2) {
            if (words1.get(0).getWeight() != null && words2.get(0).getWeight() != null) {
                return;
            }
            //词频统计（key是词，value是该词在这段句子中出现的次数）
            Map<String, AtomicInteger> frequency1 = getFrequency(words1);
            Map<String, AtomicInteger> frequency2 = getFrequency(words2);
            //如果是DEBUG模式输出词频统计信息//
            //if (log.isDebugEnabled()) {
            //    log.debug("词频统计1：\n{}", getWordsFrequencyString(frequency1));
            //    log.debug("词频统计2：\n{}", getWordsFrequencyString(frequency2));
            //}
            // 标注权重（该词出现的次数）        
            words1.parallelStream().forEach(word -> word.setWeight(frequency1.get(word.getName()).floatValue()));
            words2.parallelStream().forEach(word -> word.setWeight(frequency2.get(word.getName()).floatValue()));
        }

        /**
         * 统计词频     * @return 词频统计图
         */
        private static Map<String, AtomicInteger> getFrequency(List<Word> words) {
            Map<String, AtomicInteger> freq = new HashMap<>();
            //这步很帅哦        
            words.forEach(i -> freq.computeIfAbsent(i.getName(), k -> new AtomicInteger()).incrementAndGet());
            return freq;
        }

        /**
         * 输出：词频统计信息
         */
        private static String getWordsFrequencyString(Map<String, AtomicInteger> frequency) {
            StringBuilder str = new StringBuilder();
            if (frequency != null && !frequency.isEmpty()) {
                AtomicInteger integer = new AtomicInteger();
                frequency.entrySet().stream().sorted((a, b) -> b.getValue().get() - a.getValue().get()).forEach(i -> str.append("\t").append(integer.incrementAndGet()).append("、").append(i.getKey()).append("=").append(i.getValue()).append("\n"));
            }
            str.setLength(str.length() - 1);
            return str.toString();
        }

        /**
         * 构造权重快速搜索容器
         */
        protected static Map<String, Float> getFastSearchMap(List<Word> words) {
            if (CollectionUtils.isEmpty(words)) {
                return Collections.emptyMap();
            }
            Map<String, Float> weightMap = new ConcurrentHashMap<>(words.size());
            words.parallelStream().forEach(i -> {
                if (i.getWeight() != null) {
                    weightMap.put(i.getName(), i.getWeight());
                } else {
                    log.error("no word weight info:" + i.getName());
                }
            });
            return weightMap;
        }
    }

    /**
     * jdk没有AtomicFloat，写一个
     */
    private static class AtomicFloat extends Number {
        private static final long serialVersionUID = -8424768216812192051L;
        private final AtomicInteger bits;

        public AtomicFloat() {
            this(0f);
        }

        public AtomicFloat(float initialValue) {
            bits = new AtomicInteger(Float.floatToIntBits(initialValue));
        }    //叠加

        public final float addAndGet(float delta) {
            float expect;
            float update;
            do {
                expect = get();
                update = expect + delta;
            } while (!this.compareAndSet(expect, update));
            return update;
        }

        public final float getAndAdd(float delta) {
            float expect;
            float update;
            do {
                expect = get();
                update = expect + delta;
            } while (!this.compareAndSet(expect, update));
            return expect;
        }

        public final float getAndDecrement() {
            return getAndAdd(-1);
        }

        public final float decrementAndGet() {
            return addAndGet(-1);
        }

        public final float getAndIncrement() {
            return getAndAdd(1);
        }

        public final float incrementAndGet() {
            return addAndGet(1);
        }

        public final float getAndSet(float newValue) {
            float expect;
            do {
                expect = get();
            } while (!this.compareAndSet(expect, newValue));
            return expect;
        }

        public final boolean compareAndSet(float expect, float update) {
            return bits.compareAndSet(Float.floatToIntBits(expect), Float.floatToIntBits(update));
        }

        public final void set(float newValue) {
            bits.set(Float.floatToIntBits(newValue));
        }

        public final float get() {
            return Float.intBitsToFloat(bits.get());
        }

        @Override
        public float floatValue() {
            return get();
        }

        @Override
        public double doubleValue() {
            return (double) floatValue();
        }

        @Override
        public int intValue() {
            return (int) get();
        }

        @Override
        public long longValue() {
            return (long) get();
        }

        @Override
        public String toString() {
            return Float.toString(get());
        }
    }

}



