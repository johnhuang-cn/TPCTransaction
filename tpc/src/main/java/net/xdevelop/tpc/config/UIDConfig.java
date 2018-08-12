package net.xdevelop.tpc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.xdevelop.snowflake.SnowflakeUidGenerator;

/**
 * Enable snowflake uid generator
 * 
 * @author John.Huang
 */
@Configuration
public class UIDConfig {
	@Bean
	public SnowflakeUidGenerator transactionUidGenerator() {
		long workerId = SnowflakeUidGenerator.getWorkerIdByIP(24);
	    return new SnowflakeUidGenerator(workerId);
	}
}
