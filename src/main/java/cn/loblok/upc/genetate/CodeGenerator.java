package cn.loblok.upc.genetate;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
/**
 * 代码生成器
 */
public class CodeGenerator {

    // 数据库配置
    private static final String URL = "jdbc:mysql://localhost:3306/upc?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";
    private static final String DRIVER_NAME = "com.mysql.cj.jdbc.Driver";

    public static void main(String[] args) {
        FastAutoGenerator.create(new DataSourceConfig.Builder(URL, USERNAME, PASSWORD))
                .globalConfig(builder -> builder
                        .author("loblok") // 设置作者
                        .outputDir(System.getProperty("user.dir") + "/src/main/java") // 输出目录
                        .disableOpenDir() // 禁止打开输出目录
                )
                .packageConfig(builder -> builder
                        .parent("cn.loblok.upc") // 父包名
                        .entity("entity") // Entity包名
                        .mapper("mapper") // Mapper包名
                        .service("service") // Service包名
                        .serviceImpl("service.impl") // ServiceImpl包名
                        .controller("controller") // Controller包名（虽然需求中没有提到，但保留以备后续使用）
                )
                .strategyConfig(builder -> builder
                        // 指定表名（只生成你需要的）
                        .addInclude("user_items"
                                )
                        
                        // 实体策略配置
                        .entityBuilder()
                        .enableLombok() // 开启 Lombok（推荐）
                        .naming(NamingStrategy.underline_to_camel) // 驼峰命名
                        .columnNaming(NamingStrategy.underline_to_camel) // 字段驼峰命名
                        .enableTableFieldAnnotation() // 生成 @TableId, @TableField 等注解
                        
                        // Mapper策略配置
                        .mapperBuilder()
                        .enableBaseResultMap() // 启用 BaseResultMap
                        .enableBaseColumnList() // 启用 BaseColumnList
                        
                        // Service策略配置
                        .serviceBuilder()
                        .formatServiceFileName("%sService") // service接口命名规则
                        .formatServiceImplFileName("%sServiceImpl") // service实现类命名规则
                        
                        // Controller策略配置
                        .controllerBuilder()
                        .enableRestStyle() // 使用@RestController
                        .enableHyphenStyle() // 使用连字符风格
                )
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板
                .execute(); // 执行
    }
}