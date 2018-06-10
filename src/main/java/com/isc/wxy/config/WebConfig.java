package com.isc.wxy.config;


import com.isc.wxy.access.AccessInterceptor;
import com.isc.wxy.access.PermissionInterceptor;
import com.isc.wxy.access.SmoothBurstyInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {
	

	
	@Autowired
	AccessInterceptor accessInterceptor;

	@Autowired
	PermissionInterceptor permissionInterceptor;
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		 registry.addInterceptor(accessInterceptor)
				.excludePathPatterns("/user/login**").excludePathPatterns("/error").addPathPatterns("/**");

		registry.addInterceptor(permissionInterceptor).excludePathPatterns("/user/login**").
				excludePathPatterns("/error").addPathPatterns("/**");
		// 多个拦截器组成一个拦截器链
		registry.addInterceptor(new SmoothBurstyInterceptor(100, SmoothBurstyInterceptor.LimitType.DROP))
				.addPathPatterns("/{path}/do_miaosha");
		//限流可配置为SmoothBurstyInterceptor.LimitType.DROP丢弃请求或者SmoothBurstyInterceptor.LimitType.WAIT等待，100为每秒的速率

		super.addInterceptors(registry);

	}
	
}
