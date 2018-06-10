package com.isc.wxy.service;

import com.isc.wxy.domain.User;
import com.isc.wxy.vo.ServerResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by XY W on 2018/5/18.
 */
public interface UserService {
    public static final String COOKI_NAME_TOKEN = "token";
    ServerResponse<User> managelogin (HttpServletResponse response,Integer id,String password);
    ServerResponse<User> login (HttpServletResponse response,Integer id,String password);
   // User getByToken(HttpServletResponse response, String token);
    User getUser(HttpServletRequest request, HttpServletResponse response);
    ServerResponse<String> loginout (HttpServletRequest request);
    ServerResponse<String> register (User user);
    ServerResponse<String> checkValid(String str,String type);
    ServerResponse <String> selectQuestion(String username);
    ServerResponse <String> checkAnswer(String username,String question,String answer);
    ServerResponse <String> forgetResetPassword(HttpServletRequest request,HttpServletResponse response,
                                          String username,String passwordNew);
    ServerResponse<String> resetPassword(HttpServletRequest request,HttpServletResponse response,
                                         String passWordOld,String passWordNew,User user);
    ServerResponse<User> updateInformation(HttpServletRequest request, HttpServletResponse response,
                                           User user);
    ServerResponse checkAdminRole(User user);
}
