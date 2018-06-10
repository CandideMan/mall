package com.isc.wxy.access;

import com.alibaba.fastjson.JSON;
import com.isc.wxy.domain.User;
import com.isc.wxy.redis.RedisService;
import com.isc.wxy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {
	
	@Autowired
	UserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
			User user = userService.getUser(request, response);
			String referer=request.getHeader("referer");
			System.out.print("成功拦截");
		if(user==null) {
			String url ="/user/login";
			response.sendRedirect(url);
			return false;
		}
		else {
			if (UserContext.getUser() == null) {
			UserContext.setUser(user);
			}
		}
		if(referer==null||!referer.contains(request.getServerName()))
			{
				return  false;
			}
		return true;
	}
	





	
}
