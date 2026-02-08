package com.src.main.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthModuleConfig {

	@Bean
	public static BeanFactoryPostProcessor markOtpSenderRouterAsPrimary() {
		return new BeanFactoryPostProcessor() {
			@Override
			public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
				if (beanFactory.containsBeanDefinition("otpSenderRouter")) {
					beanFactory.getBeanDefinition("otpSenderRouter").setPrimary(true);
				}
			}
		};
	}
}
