package cn.loblok.upc.common.config;

import cn.loblok.upc.common.utils.RedisUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
// 通过 @Import 引入，配合 @ConditionalOnClass 使用会更安全
@Import({RedisAutoConfig.class, RedisUtils.class})
public class CommonCommonConfig {
}