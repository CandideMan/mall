package com.isc.wxy.controller.backend;

import com.isc.wxy.access.UserContext;
import com.isc.wxy.domain.User;
import com.isc.wxy.enums.ResponseCode;
import com.isc.wxy.service.CategoryService;
import com.isc.wxy.service.UserService;
import com.isc.wxy.vo.ServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by XY W on 2018/5/20.
 */
@RestController
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    UserService userService;
    @Autowired
    CategoryService categoryService;

    //增加一个类目
    @RequestMapping("/manage/category/add_category")
    public ServerResponse addCategory(String categoryName,
                                      @RequestParam(value = "parentId" ,defaultValue = "0") Integer parentId)
    {
        User user= UserContext.getUser();
        {
            if(user==null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
            }
        }
        if(userService.checkAdminRole(user).isSuccess()){
            return categoryService.addCategory(categoryName,parentId);
        }
        else
        {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }
    //重置一个类目的名字
    @RequestMapping("/manage/category/set_category_name")
    public ServerResponse setCategoryName(Integer categoryId,String categoryName){
        User user= UserContext.getUser();
        {
            if(user==null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
            }
        }
        if(userService.checkAdminRole(user).isSuccess()){
            return categoryService.addCategoryName(categoryId, categoryName);
        }
        else
        {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    //获取一个类目的平级子目录不递归
    @RequestMapping("/manage/category/get_children_category")
    public ServerResponse getChildrenParallelCategory(@RequestParam(value = "categoryId",defaultValue = "0")
                                                                  Integer categoryId)
    {
        User user= UserContext.getUser();{
            if(user==null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
            }
        }
        if(userService.checkAdminRole(user).isSuccess()){
            return categoryService.getChildrenParallelCategory(categoryId);
        }
        else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    //递归获取一个类目和其子目录
    @RequestMapping("/manage/category/get_deep_children_category")
    public ServerResponse getDeepChildrenCategory(@RequestParam(value = "categoryId",defaultValue = "0")
                                                              Integer categoryId)
    {
        User user= UserContext.getUser();{
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
    }
        if(userService.checkAdminRole(user).isSuccess()){
            return categoryService.getDeepChildrenCategory(categoryId);
        }
        else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }
}
