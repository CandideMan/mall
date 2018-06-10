package com.isc.wxy.controller.backend;

import com.isc.wxy.common.Const;
import com.isc.wxy.domain.User;
import com.isc.wxy.service.UserService;
import com.isc.wxy.vo.ServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by geely
 */

@Controller
@RequestMapping("/manage/user")
public class UserManageController {

    @Autowired
    private UserService userService;

    @RequestMapping(value="/login",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(Integer id, String password, HttpServletResponse response){
        return  userService.managelogin(response,id,password);

    }

}
