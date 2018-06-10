package com.isc.wxy;

import com.isc.wxy.dao.UserDao;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@Configuration
@EnableAutoConfiguration(exclude = {MultipartAutoConfiguration.class})
@MapperScan("com.isc.wxy.dao.*")
public class MallApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(MallApplication.class, args);

    }
    @Override//为了打包springboot项目
     protected SpringApplicationBuilder configure( SpringApplicationBuilder builder)
    { return builder.sources(this.getClass()); }

    @Bean(name = "multipartResolver")
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("UTF-8");
        resolver.setResolveLazily(true);//resolveLazily属性启用是为了推迟文件解析，以在在UploadAction中捕获文件大小异常
        resolver.setMaxInMemorySize(40960);
        resolver.setMaxUploadSize(5 * 1024 * 1024);//上传文件大小 5M 5*1024*1024
        return resolver;
    }
}
