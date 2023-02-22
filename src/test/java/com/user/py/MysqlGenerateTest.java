package com.user.py;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

/**
 * @Author ice
 * @Date 2023/2/13 17:14
 * @Description: TODO
 */
//@SpringBootTest
public class MysqlGenerateTest {
    public static void main(String[] args) {
        TestDemo("jdbc:mysql://localhost:3306/user_center?serverTimezone=GMT%2B8","root","pwb2001");
    }
    //@Test
    public static void TestDemo(String url,String username,String password){
        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> {
                    builder.author("ice") // 设置作者
                            .enableSwagger() // 开启 swagger 模式
                            .fileOverride() // 覆盖已生成文件
                            .outputDir("D:\\JavaUser\\pyhb-friend\\src\\main\\java"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.user.py") // 设置父包名
                            .moduleName("comment_reply") // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, "D:\\JavaUser\\pyhb-friend\\src\\main\\java\\com\\user\\py")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("comment_reply");
                // 设置需要生成的表名
                             // 设置过滤表前缀
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }


}
