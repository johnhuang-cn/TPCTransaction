package net.xdevelop.tpc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import net.xdevelop.tpc.config.ExecutorConfig;
import net.xdevelop.tpc.config.RibbonRuleConfig;
import net.xdevelop.tpc.config.TPCAspectAutoConfig;
import net.xdevelop.tpc.config.UIDConfig;

/**
 * Enable TPC configs
 * 
 * @author John.Huang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({ TPCTransactionController.class, 
		UIDConfig.class, 
		FeignInterceptor.class, 
		TPCTransactionManager.class,
		TPCExecutorManager.class,
		ExecutorConfig.class,
		TPCAspectAutoConfig.class,
		RibbonRuleConfig.class
		})
@Configuration
public @interface EnableTPC {

}
