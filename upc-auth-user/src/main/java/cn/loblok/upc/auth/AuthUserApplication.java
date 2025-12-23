package cn.loblok.upc.auth;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
/**
 * @author loblok
 */
@SpringBootApplication
@EnableAsync // ← 启用异步
@MapperScan("cn.loblok.upc.auth.mapper")
@EnableDiscoveryClient // 开启 Consul 服务发现
@EnableFeignClients(basePackages = "cn.loblok.upc.api")
public class AuthUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthUserApplication.class, args);
    }
}