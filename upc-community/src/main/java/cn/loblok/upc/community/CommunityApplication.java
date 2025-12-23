package cn.loblok.upc.community;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient // 开启 Consul 服务发现
@EnableFeignClients(basePackages = "cn.loblok.upc.api")
@MapperScan("cn.loblok.upc.community.mapper")
public class CommunityApplication {

    public static void main(String[] args) {
        System.out.println("Feign 版本: " +org.springframework.cloud.openfeign.FeignClientFactoryBean.class.getProtectionDomain().getCodeSource().getLocation());

        SpringApplication.run(CommunityApplication.class, args);
    }
}