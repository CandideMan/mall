package com.isc.wxy.controller.backend;

import com.isc.wxy.access.UserContext;
import com.isc.wxy.domain.Product;
import com.isc.wxy.domain.User;
import com.isc.wxy.enums.ResponseCode;
import com.isc.wxy.service.FileService;
import com.isc.wxy.service.ProductService;
import com.isc.wxy.service.UserService;
import com.isc.wxy.utils.PropertiesUtil;
import com.isc.wxy.vo.ServerResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by XY W on 2018/5/21.
 */
@Controller
@RequestMapping("manage/product")
public class ProductManageController {
    @Autowired
    UserService userService;
    @Autowired
    ProductService productService;
    @Autowired
    FileService fileService;

    @Controller
    @RequestMapping("/")
    public class IndexController {
        public  String index() {
            return "index";
        }
    }

    @PostMapping("/save_product")
    @ResponseBody
    public ServerResponse productSave(Product product){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        if(userService.checkAdminRole(user).isSuccess()){
            return productService.saveOrUpdateProduct(product);
        }
        else
        {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    @PostMapping("/set_sale_status")
    @ResponseBody
    public ServerResponse setSaleStatus(Integer productId,Integer status){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        if(userService.checkAdminRole(user).isSuccess()){
            return productService.setSaleStatus(productId,status);
        }
        else
        {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    @GetMapping("/product_detail")
    @ResponseBody
    public ServerResponse getDetail(Integer productId){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        if(userService.checkAdminRole(user).isSuccess()){
            return productService.manageProductDetail(productId);
        }
        else
        {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    @GetMapping("/product_list")
    @ResponseBody
    public ServerResponse getList(@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                  @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        if(userService.checkAdminRole(user).isSuccess()){
            return productService.getProductList(pageNum, pageSize);
        }
        else
        {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    @GetMapping("/product_serach")
    @ResponseBody
    public ServerResponse productSerach(Integer productId,String productName,
                                  @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                  @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        if(userService.checkAdminRole(user).isSuccess()){
            return productService.serachProduct(productName,productId,pageNum,pageSize);
        }
        else
        {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    @PostMapping("/upload" )
    @ResponseBody
    public ServerResponse upload(@RequestParam(value = "upload_file") MultipartFile file, HttpServletRequest request){

    User user= UserContext.getUser();
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getMsg());
        }
        if(userService.checkAdminRole(user).isSuccess()) {
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = fileService.upload(file, path);
            if(StringUtils.isBlank(targetFileName)){
                return ServerResponse.createByErrorMessage("上传错误");
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            Map<String, String> fileMap = new HashMap<>();
            fileMap.put("uri", targetFileName);
            fileMap.put("url", url);
            return ServerResponse.createBySuccess(fileMap);
        }
        else
        {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    @PostMapping("/rich_text_img_upload" )
    @ResponseBody
    public Map richTextImgUpload(@RequestParam(value = "upload_file") MultipartFile file,
                                 HttpServletRequest request, HttpServletResponse response){
        Map resultMap=new HashMap<>();

        User user= UserContext.getUser();
        if(user==null){
           resultMap.put("success",false);
           resultMap.put("msg","请登录管理员");
           return resultMap;
        }
        if(userService.checkAdminRole(user).isSuccess()) {
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = fileService.upload(file, path);
            if(StringUtils.isBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败");
                return resultMap;
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            resultMap.put("file_path",url);
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        }
        else
        {
            resultMap.put("success",false);
            resultMap.put("msg","无权限操作");
            return resultMap;
        }
    }


}
