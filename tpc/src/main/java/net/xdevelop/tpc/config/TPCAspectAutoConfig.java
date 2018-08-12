package net.xdevelop.tpc.config;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.Advice;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import net.xdevelop.tpc.aspect.TPCTransactionalAspect;

/**
 * Enable TPCTransactional annotation aspect.
 * 
 * @author John.Huang
 */
@Configuration
@ConditionalOnClass({ TPCTransactionalAspect.class, EnableAspectJAutoProxy.class, Aspect.class, Advice.class })
public class TPCAspectAutoConfig {
	@Bean
	public TPCTransactionalAspect tpcTransactionalAspect() {
		return new TPCTransactionalAspect();
	}
}
