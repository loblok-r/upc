package cn.loblok.upc.worker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient // 开启 Consul 服务发现
@EnableFeignClients(basePackages = "cn.loblok.upc.api")
@MapperScan("cn.loblok.upc.worker.mapper")
public class WorkerApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(WorkerApplication.class, args);
    }
}