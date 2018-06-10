package com.isc.wxy.service.impl;

import com.isc.wxy.access.UserContext;
import com.isc.wxy.common.Const;
import com.isc.wxy.dao.UserDao;
import com.isc.wxy.domain.User;
import com.isc.wxy.enums.ResponseCode;
import com.isc.wxy.enums.RoleEnum;
import com.isc.wxy.exception.MallException;
import com.isc.wxy.redis.RedisService;
import com.isc.wxy.redis.UserKey;
import com.isc.wxy.service.UserService;
import com.isc.wxy.utils.MD5Util;
import com.isc.wxy.utils.UUIDUtil;
import com.isc.wxy.vo.ServerResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 * Created by XY W on 2018/5/18.
 */
@Service
public class UserServiceImpl implements UserService {
   @Autowired
   UserDao userDao;
   @Autowired
    RedisService redisService;

   //用户登陆
    @Override
    public ServerResponse<User> login(HttpServletResponse response, Integer id, String password) {
        if(id==null||password==null){
            throw new MallException(ResponseCode.ERROR);
        }
        User user =userDao.selectByPrimaryKey(id);
        if(user==null){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        //验证密码
        String dbPass=user.getPassword();
        String pass=MD5Util.MD5EncodeUtf8(password);
        if(userDao.checkPassword(pass,id)!=1)
        {
            return ServerResponse.createByErrorMessage("密码错误");
        }
       User oldUser= redisService.get(UserKey.currentToken,user.getPhone(),User.class);
        if(oldUser!=null){
            return ServerResponse.createByErrorMessage("用户名已登录");
        }
        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        redisService.set(UserKey.currentToken,user.getPhone(),user);
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return  ServerResponse.createBySuccessMessage("登陆成功",user);
    }

    //管理员登录
    public ServerResponse<User> managelogin(HttpServletResponse response, Integer id, String password){
        if(id==null||password==null){
            throw new MallException(ResponseCode.ERROR);
        }
        User user =userDao.selectByPrimaryKey(id);
        if(user==null){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        //验证密码
        String dbPass=user.getPassword();
        String pass=MD5Util.MD5EncodeUtf8(password);
        if(userDao.checkPassword(pass,id)!=1)
        {
            return ServerResponse.createByErrorMessage("密码错误");
        }
        if(!user.getRole().equals(RoleEnum.ADMIN_ROLE.getRole()))
        {
            return ServerResponse.createByErrorMessage("您不是管理员用户");
        }
        User oldUser= redisService.get(UserKey.token,user.getPhone(),User.class);
        if(oldUser!=null){
            return ServerResponse.createByErrorMessage("用户名已登录");
        }
        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        redisService.set(UserKey.currentToken,user.getPhone(),user);
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return  ServerResponse.createBySuccessMessage("登陆成功",user);
    }
    //退出登陆
    @Override
    public ServerResponse<String> loginout(HttpServletRequest request) {
            String token =getToken(request);
            User user=redisService.get(UserKey.token,token,User.class);
            redisService.delete(UserKey.token,token);
            redisService.delete(UserKey.currentToken,user.getPhone());
            return ServerResponse.createBySuccessMessage("退出成功");
    }

    //注册
    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse validResponse =this.checkValid(user.getUsername(),Const.USERNAME);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        validResponse =this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        user.setRole(RoleEnum.CUSTOM_ROLE.getRole());
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
      int  result=userDao.insert(user);
        if(result==0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return  ServerResponse.createBySuccessMessage("注册成功");
    }

    //检查输入的用户名和邮箱是否有效,存在返回失败
    public ServerResponse<String> checkValid(String str,String type)
    {
        if(org.apache.commons.lang3.StringUtils.isNoneBlank(type))
        {
            if(Const.USERNAME.equals(type))
            {
                int result=userDao.checkUsername(str);
                if(result>0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if(Const.EMAIL.equals(type))
            {
               int result=userDao.checkEmail(str);
                if(result>0) {
                    return ServerResponse.createByErrorMessage("Email已存在");
                }
            }
        }else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    //获取密码提示问题
    public ServerResponse <String> selectQuestion(String username)
    {
        ServerResponse validResponse =this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question=userDao.selectQuestionByUsername(username);
        if(org.apache.commons.lang3.StringUtils.isNoneBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    //检查密码找回的问题是否正确
    public ServerResponse <String> checkAnswer(String username,String question,String answer)
    {
        int result=userDao.checkAnswer(username, question, answer);
        if(result>0){
            //回答正确
            String forgetToken=UUIDUtil.uuid();
            redisService.set(UserKey.forgettoken,username,forgetToken);
            return ServerResponse.createBySuccessMessage("回答正确");
        }
        return ServerResponse.createByErrorMessage("答案错误");
    }

    @Transactional
    //忘记密码重置密码
    public ServerResponse <String> forgetResetPassword(HttpServletRequest request,HttpServletResponse response,
                                                 String username,String passwordNew){
        if(org.apache.commons.lang3.StringUtils.isBlank(username)
                ||org.apache.commons.lang3.StringUtils.isBlank(passwordNew))
        {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        ServerResponse validResponse =this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String forgetToken=redisService.get(UserKey.forgettoken,username,String.class);
        if(org.apache.commons.lang3.StringUtils.isBlank(forgetToken))
        {
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }
        passwordNew=MD5Util.MD5EncodeUtf8(passwordNew);
       int result= userDao.updatePasswordByUsername(username,passwordNew);
        if(result>0)
        {
            return ServerResponse.createBySuccessMessage("修改密码成功");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    //登陆后重置密码
    public ServerResponse<String> resetPassword(HttpServletRequest request,HttpServletResponse response,
            String passWordOld,String passWordNew,User user)
    {
        //防止横向越权，校验旧密码一定是这个用户
        int result=userDao.checkPassword(MD5Util.MD5EncodeUtf8(passWordOld),user.getId());
        if(result==0){
            return  ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passWordNew));
        result=userDao.updateByPrimaryKeySelective(user);
        if(result>0){
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    //更新用户信息
    public ServerResponse<User> updateInformation(HttpServletRequest request, HttpServletResponse response,
                                                  User user){
        //username和id不能被更新 并且email和phone要进行校验，不能存在相同的，
        // 如果存在相同的且不是这个用户的，就不能更新
            int result=userDao.checkEmailByUserId(user.getEmail(),user.getId());
            if(result>0){
                return ServerResponse.createByErrorMessage("Email已经存在");
            }
               result=userDao.checkPhoneByUserId(user.getPhone(),user.getId());
             if(result>0){
            return ServerResponse.createByErrorMessage("电话号码已经存在");
        }
             User upUser=redisService.get(UserKey.currentToken,user.getPhone(),User.class);
             upUser.setEmail(user.getEmail());
             upUser.setPhone(user.getPhone());
             upUser.setQuestion(user.getQuestion());
             upUser.setAnswer(user.getAnswer());
             result=userDao.updateByPrimaryKeySelective(upUser);
             if(result>0){
                 processCache(request,response,upUser);
                 return ServerResponse.createBySuccessMessage("更新成功",upUser);
             }
             return ServerResponse.createByErrorMessage("更新失败");
    }

    //校验是否是管理员
    public ServerResponse checkAdminRole(User user)
    {
        if(user!=null&&Integer.parseInt(user.getRole())==RoleEnum.ADMIN_ROLE.getCode()){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    private void processCache(HttpServletRequest request,HttpServletResponse response,
                             User user){
        //处理缓存
        String paramToken = request.getParameter(UserServiceImpl.COOKI_NAME_TOKEN);
        String cookieToken = getCookieValue(request, UserServiceImpl.COOKI_NAME_TOKEN);
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        if(org.apache.commons.lang3.StringUtils.isNoneBlank(token)) {
            redisService.set(UserKey.token, token, user);
        }
        redisService.set(UserKey.currentToken,user.getPhone(),user);
    }

    //用户第一次登陆后把用户信息存入redis和cookie中
    private void addCookie(HttpServletResponse response, String token,User user) {
        redisService.set(UserKey.token, token, user);
        Cookie cookie = new Cookie(COOKI_NAME_TOKEN, token);
        cookie.setMaxAge(UserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    //根据用户唯一标识获取用户信息,并且延长缓存时间
    private User getByToken(HttpServletResponse response, String token) {
        if(StringUtils.isEmpty(token)) {
            return null;
        }
        User user = redisService.get(UserKey.token, token,User.class);
        //延长有效期
        if(user != null) {
            addCookie(response, token, user);
        }
        return user;
    }

    //不知道唯一标识，根据用户请求返回用户信息
    public User getUser(HttpServletRequest request, HttpServletResponse response) {
        String token = getToken(request);
        return getByToken(response, token);
    }
    private String getToken(HttpServletRequest request)
    {
        String paramToken = request.getParameter(UserService.COOKI_NAME_TOKEN);
        String cookieToken = getCookieValue(request, UserService.COOKI_NAME_TOKEN);
        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        return  token;
    }

    //根据名字获取cookie值
    private String getCookieValue(HttpServletRequest request, String cookiName) {
        Cookie[]  cookies = request.getCookies();
        if(cookies == null || cookies.length <= 0){
            return null;
        }
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(cookiName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
