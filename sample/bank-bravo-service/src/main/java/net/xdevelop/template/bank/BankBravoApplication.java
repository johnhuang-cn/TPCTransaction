package net.xdevelop.template.bank;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import net.xdevelop.tpc.EnableTPC;

@SpringBootApplication
@EnableEurekaClient
@MapperScan("net.xdevelop.template.bank.mapper")
@PropertySource(value = {"classpath:druid.properties", "classpath:mybatis.properties"})
@EnableTransactionManagement
@EnableAutoConfiguration
@EnableTPC
public class BankBravoApplication {
	public static void main(String[] args) {
		SpringApplication.run(BankBravoApplication.class, args);
	}
}
