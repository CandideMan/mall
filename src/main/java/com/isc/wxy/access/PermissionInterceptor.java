package com.isc.wxy.access;

import com.isc.wxy.domain.User;
import com.isc.wxy.redis.RedisService;
import com.isc.wxy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class PermissionInterceptor extends HandlerInterceptorAdapter {
	
	@Autowired
	UserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		//if(true){return true;}
			User user = userService.getUser(request, response);
			if(user!=null) {
				if(user.getRole()=="1")
				return true;
			}
			else
			{
				String url ="/user/login/";
				response.sendRedirect(url);
				return false;
			}
		String url ="/user/login/";
		response.sendRedirect(url);
		return false;
	}
	



	
}
