package com.isc.wxy.controller.portal;


import com.isc.wxy.access.UserContext;
import com.isc.wxy.dao.UserDao;
import com.isc.wxy.domain.User;
import com.isc.wxy.service.UserService;

import com.isc.wxy.vo.ServerResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Created by XY W on 2018/5/18.
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    UserDao userDao;

    @GetMapping("/login_1")
    public String do1() {
        //登录
        return  "hello";
    }
    @PostMapping("/login")
    public ServerResponse<User> doLogin(HttpServletResponse response,@RequestParam("id") Integer id,
                                        @RequestParam("password")String password ) {
        //登录
        return  userService.login(response,id,password);

    }

    //退出登录
    @PostMapping("/login_out")
    public ServerResponse<String> logout(HttpServletRequest request){
        return userService.loginout(request);
    }

    //注册
    @PostMapping("/login_register")
    public ServerResponse<String> register(User user) {
        return userService.register(user);
    }

    //校验输入的用户名或者邮箱是否有效
    @PostMapping("/checkvalid")
    public ServerResponse<String> checkValid( @RequestParam("str")String str ,
                                              @RequestParam("type")String type) {
        return userService.checkValid(str,type);
    }

    //获取用户信息
    @PostMapping("/get_user_info")
    public ServerResponse<User> getUserInfo(HttpServletRequest request,HttpServletResponse response) {
        User user= userService.getUser(request,response);
        if(user!=null) {
            return ServerResponse.createBySuccess(user);
        }
        return  ServerResponse.createByErrorMessage("用户未登录");
    }

    //获取密码提示问题
    @PostMapping("/login_get_questionTip")
    public ServerResponse<String> getQuestionTip(String username)
    {
        return userService.selectQuestion(username);
    }

    //校验密码提示问题回答是否正确
    @PostMapping("/login_check_answer")
    public ServerResponse<String> checkAnswer( @RequestParam("uername")String username,
                                               @RequestParam("question")String question,
                                               @RequestParam("answer")String answer)
    {
            return  userService.checkAnswer(username, question, answer);
    }

    //忘记密码时重置密码
    @PostMapping("/login_forget_reset_password")
    public ServerResponse<String> forget_resetPassword(HttpServletRequest request,HttpServletResponse response,
                                                       @RequestParam("uername") String username,
                                                       @RequestParam("passwordNew")String passwordNew)
    {
                return userService.forgetResetPassword(request, response, username, passwordNew);
    }

    //登录情况下重置密码
    @PostMapping("/reset_password")
    public ServerResponse<String> resetPassword(HttpServletRequest request, HttpServletResponse response,
                                                @RequestParam("passWordOld") String passWordOld,
                                                @RequestParam("passwordNew") String passwordNew)
    {
            User user= UserContext.getUser();
            if(user==null){
                return ServerResponse.createByErrorMessage("用户未登录");
            }
            return  userService.resetPassword(request,response,passWordOld,passwordNew,user);

    }

    //更新用户信息
    @PostMapping("/update_information")
    public ServerResponse<User> updateInformation(HttpServletRequest request, HttpServletResponse response,
                                                  User user){
        User currentUser=UserContext.getUser();
        if(currentUser==null)
        {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        user.setId(currentUser.getId());
        return userService.updateInformation(request, response, user);
    }


}
