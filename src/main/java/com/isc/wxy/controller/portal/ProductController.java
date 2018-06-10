package com.isc.wxy.controller.portal;

import com.github.pagehelper.PageInfo;
import com.isc.wxy.service.ProductService;
import com.isc.wxy.vo.ProductDetailVo;
import com.isc.wxy.vo.ServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by XY W on 2018/5/22.
 */
@Controller
@RequestMapping("/product")
public class ProductController {
    @Autowired
    ProductService productService;

    @RequestMapping("/detail")
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail (Integer productId){
        return productService.getproductDetail(productId);
    }

    @RequestMapping("/list")
    @ResponseBody
    public ServerResponse<PageInfo> list (@RequestParam(value = "keyword",required = false) String keyword,
                                          @RequestParam(value = "categoryId",required = false) Integer categoryId,
                                          @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                          @RequestParam(value = "pageSize",defaultValue = "10")int pageSize,
                                          @RequestParam(value = "orderBy",defaultValue = "")String orderBy)
    {
                return productService.getProductByKeyWordCategory(keyword,categoryId,pageNum,pageSize,orderBy);
    }
}
