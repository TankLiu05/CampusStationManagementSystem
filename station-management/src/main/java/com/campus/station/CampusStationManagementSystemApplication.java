package com.campus.station;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication
@SpringBootApplication(excludeName = {
        "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        "org.springframework.boot.jdbc.autoconfigure.DataSourceInitializationAutoConfiguration"
})
public class CampusStationManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampusStationManagementSystemApplication.class, args);
	}

}
