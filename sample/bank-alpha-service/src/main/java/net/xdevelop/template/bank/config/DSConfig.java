package net.xdevelop.template.bank.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;

@Configuration
public class DSConfig {
	@Bean
	@ConfigurationProperties("spring.datasource.druid.alpha")
	public DataSource dataSource(){
	    return DruidDataSourceBuilder.create().build();
	}
	
	@Bean
    public PlatformTransactionManager transactionManager (DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
    }
}
