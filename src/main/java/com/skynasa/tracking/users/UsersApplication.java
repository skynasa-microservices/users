package com.skynasa.tracking.users;

import com.skynasa.tracking.commonpackage.utils.constants.Paths;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = {Paths.ClientBasePackage, Paths.User.ServiceBasePackage})
@SpringBootApplication(scanBasePackages = {Paths.ConfigurationBasePackage, Paths.User.ServiceBasePackage})
public class UsersApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsersApplication.class, args);
	}

}
