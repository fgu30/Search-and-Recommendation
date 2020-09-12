package com.bin.spark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = {"com.bin.spark"})
@MapperScan(basePackages = "com.bin.spark.mapper")
public class SparkApplication {
    public static void main(String[] args) {
        SpringApplication.run(SparkApplication.class, args);
        System.out.println("/ \\  / \\  / \\  / \\  / \\ \n" +
                " ( S )( P )( A )( R )( K )\n" +
                "  \\_/  \\_/  \\_/  \\_/  \\_/    启动成功ლ(´ڡ`ლ)");
    }
}
