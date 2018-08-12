package net.xdevelop.template.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

import net.xdevelop.tpc.EnableTPC;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableHystrix
@EnableTPC
public class BankClientSampleApplication {
	public static void main(String[] args) {
		SpringApplication.run(BankClientSampleApplication.class, args);
	}
}
