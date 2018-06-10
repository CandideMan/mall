package com.isc.wxy.service;

import com.github.pagehelper.PageInfo;
import com.isc.wxy.domain.Product;
import com.isc.wxy.vo.ProductDetailVo;
import com.isc.wxy.vo.ServerResponse;

/**
 * Created by XY W on 2018/5/21.
 */
public interface ProductService {
    ServerResponse saveOrUpdateProduct(Product product);
    ServerResponse setSaleStatus(Integer productId,Integer status);
    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);
    ServerResponse<PageInfo> getProductList(int pageNum,int pageSize);
    ServerResponse<PageInfo> serachProduct(String productName, Integer prductId, int pageNum, int pageSize);
    ServerResponse<ProductDetailVo> getproductDetail(Integer productId);
    ServerResponse<PageInfo> getProductByKeyWordCategory(String keyWord,Integer categoryId,
                                                         int pageNum,int pageSize,String orderBy);
}
