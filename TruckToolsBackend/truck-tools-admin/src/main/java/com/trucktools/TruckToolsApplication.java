package com.trucktools;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * TruckTools 应用启动类
 * 
 * 卡车外贸综合工具平台
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@MapperScan({
    "com.trucktools.system.mapper",
    "com.trucktools.customer.mapper",
    "com.trucktools.email.mapper",
    "com.trucktools.product.mapper"
})
public class TruckToolsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TruckToolsApplication.class, args);
        System.out.println("\n" +
                "  _____              _    _____           _     \n" +
                " |_   _| __ _   _ __| | _|_   _|__   ___ | |___ \n" +
                "   | || '__| | | / _` |/ / | |/ _ \\ / _ \\| / __|\n" +
                "   | || |  | |_| | (_| |_|  | | (_) | (_) | \\__ \\\n" +
                "   |_||_|   \\__,_|\\__,_(_)  |_|\\___/ \\___/|_|___/\n" +
                "                                                 \n" +
                "  :: TruckTools :: 卡车外贸综合工具平台 :: v1.0.0 ::\n" +
                "\n" +
                "  启动成功！访问地址:\n" +
                "  - API文档: http://localhost:8080/doc.html\n" +
                "  - 健康检查: http://localhost:8080/actuator/health\n");
    }
}

