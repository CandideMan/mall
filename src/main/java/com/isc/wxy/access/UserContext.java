package com.isc.wxy.access;


import com.isc.wxy.domain.User;
import com.isc.wxy.redis.RedisService;
import com.isc.wxy.redis.UserKey;
import org.springframework.beans.factory.annotation.Autowired;

public class UserContext {


	private static ThreadLocal<User> userHolder = new ThreadLocal<User>();
	
	public static void setUser(User user) {
		userHolder.set(user);
	}
	
	public static User getUser() {
		return userHolder.get();
	}

}
