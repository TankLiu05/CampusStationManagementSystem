package com.campus.station;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用入口类：启动校园站点管理系统后端服务。
 * 现已启用数据源与 JPA 自动配置以连接 MySQL。
 */
@SpringBootApplication
public class CampusStationManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusStationManagementSystemApplication.class, args);
    }

}
