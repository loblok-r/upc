package cn.loblok.upc;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
/**
 * @author loblok
 */
@SpringBootApplication
@EnableAsync // ← 启用异步
@MapperScan("cn.loblok.upc.mapper")
public class UpcApplication {
    public static void main(String[] args) {
        SpringApplication.run(UpcApplication.class, args);
    }
}