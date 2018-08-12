package net.xdevelop.tpc.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.ZoneAvoidanceRule;

import net.xdevelop.tpc.TPCRuler;

/**
 * Enable TPCRule
 * 
 * @author John.Huang
 */
@Configuration
public class RibbonRuleConfig {
	
	@Bean
	@Scope("prototype")
    public IRule ribbonRule() {
		TPCRuler.setRuleClass(ZoneAvoidanceRule.class);
        return new TPCRuler();
    }
}
