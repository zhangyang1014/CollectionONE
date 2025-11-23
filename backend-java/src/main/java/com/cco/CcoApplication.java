package com.cco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CCO 催收系统主应用入口
 * 
 * @author CCO Team
 * @version 1.0.0
 */
@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
})
public class CcoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CcoApplication.class, args);
        System.out.println("\n======================================");
        System.out.println("   CCO System Started Successfully   ");
        System.out.println("   API Documentation: http://localhost:8080/api/v1");
        System.out.println("======================================\n");
    }

}

