package net.xdevelop.tpc.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import net.xdevelop.tpc.TPCExecutorManager;

/**
 * Start the transaction monitor thread.
 * 
 * @author John.Huang
 */
@Configuration
public class ExecutorConfig {
	@Autowired
	TPCExecutorManager em;
	
	@PostConstruct
	public void postConstruct() {
		em.start();
	}
}
