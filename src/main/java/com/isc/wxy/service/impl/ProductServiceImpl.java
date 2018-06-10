package com.isc.wxy.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.isc.wxy.common.Const;
import com.isc.wxy.dao.CategoryDao;
import com.isc.wxy.dao.ProductDao;
import com.isc.wxy.domain.Category;
import com.isc.wxy.domain.Product;
import com.isc.wxy.enums.ProductStatusEnum;
import com.isc.wxy.enums.ResponseCode;
import com.isc.wxy.service.CategoryService;
import com.isc.wxy.service.ProductService;
import com.isc.wxy.utils.DateTimeUtil;
import com.isc.wxy.utils.PropertiesUtil;
import com.isc.wxy.vo.ProductDetailVo;
import com.isc.wxy.vo.ProductListVo;
import com.isc.wxy.vo.ServerResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XY W on 2018/5/21.
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductDao productDao;
    @Autowired
    CategoryDao categoryDao;
    @Autowired
    CategoryService categoryService;
    //保存或者更新产品
    @Transactional
    public ServerResponse saveOrUpdateProduct(Product product){
        if(product==null){
            return ServerResponse.createByErrorMessage("新增或者更新产品参数不正确");
        }
        if(StringUtils.isNotBlank(product.getSubImages())){
            String[]subImageArray=product.getSubImages().split(",");
            if(subImageArray.length>0){
                product.setMainImage(subImageArray[0]);
            }
        }
        if(product.getId()!=null){
          int res=  productDao.updateByPrimaryKey(product);
            if(res>0){
                return ServerResponse.createBySuccessMessage("更新产品成功");
            }
            else{
                return ServerResponse.createByErrorMessage("更新产品失败");
            }
        }
        else {
            int res= productDao.insert(product);
            if(res>0){
                return ServerResponse.createBySuccessMessage("新增产品成功");
            }
            else{
                return ServerResponse.createByErrorMessage("新增产品失败");
            }
        }
    }

    //修改产品状态
    public ServerResponse setSaleStatus(Integer productId,Integer status){
        if(productId==null||status==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.PARAM_ERROR.getCode(),ResponseCode.PARAM_ERROR.getMsg());
        }
        Product product=new Product();
        product.setId(productId);
        product.setStatus(status);
        int res=productDao.updateByPrimaryKeySelective(product);
        if(res>0){
            return ServerResponse.createByErrorMessage("修改产品状态成功");
        }
        return ServerResponse.createByErrorMessage("修改产品状态失败");
    }
    //后台获取商品详情
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId){
        if(productId==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.PARAM_ERROR.getCode(),ResponseCode.PARAM_ERROR.getMsg());
        }
        Product product=productDao.selectByPrimaryKey(productId);
        if(product==null){
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        ProductDetailVo productDetailVo=assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }
    //商品列表
    public ServerResponse<PageInfo> getProductList(int pageNum,int pageSize){
        //startpage  记录开始
        //填充sql查询逻辑
        //pagehepler收尾
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList=productDao.selectList();
        List<ProductListVo>productListVoList=new ArrayList<>();
        for(Product productItem:productList){
            ProductListVo productListVo=assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult =new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }
    //搜索商品
    public ServerResponse<PageInfo> serachProduct(String productName,Integer prductId,int pageNum,int pageSize)
    {
        PageHelper.startPage(pageNum,pageSize);
        if(StringUtils.isNoneBlank(productName)){
            productName=new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product>productList=productDao.selectByNameAndProductId(productName,prductId);
        List<ProductListVo>productListVoList=new ArrayList<>();
        for(Product productItem:productList){
            ProductListVo productListVo=assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult =new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }
    //用户获取商品详情
    public ServerResponse<ProductDetailVo> getproductDetail(Integer productId){
        if(productId==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.PARAM_ERROR.getCode(),ResponseCode.PARAM_ERROR.getMsg());
        }
        Product product=productDao.selectByPrimaryKey(productId);
        if(product==null||product.getStatus()!= ProductStatusEnum.UP.getCode()){
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        ProductDetailVo productDetailVo=assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    public ServerResponse<PageInfo> getProductByKeyWordCategory(String keyWord,Integer categoryId,
                                                                int pageNum,int pageSize,String orderBy){
        if(StringUtils.isBlank(keyWord)&&categoryId==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.PARAM_ERROR.getCode(),ResponseCode.PARAM_ERROR.getMsg());
        }
        List<Integer> categoryIdList=new ArrayList<>();
        if(categoryId!=null){
            Category category =categoryDao.selectByPrimaryKey(categoryId);
            if(category==null&&StringUtils.isBlank(keyWord)){
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVo>productListVoList=new ArrayList<>();
                PageInfo pageInfo=new PageInfo(productListVoList);
                return ServerResponse.createBySuccess(pageInfo);
            }
            //递归获取这个分类下面的所有分类
            categoryIdList =categoryService.getDeepChildrenCategory(categoryId).getData();
        }
        if(StringUtils.isNotBlank(keyWord)){
            keyWord=new StringBuilder().append("%").append(keyWord).append("/%").toString();
        }
        PageHelper.startPage(pageNum,pageSize);
        //排序处理
        if(StringUtils.isNotBlank(orderBy)){
            if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy))
            {
                String[]orderByArray=orderBy.split("_");
                PageHelper.orderBy(orderByArray[0]+" "+orderByArray[1]);
            }
        }
        List<Product> productList =productDao.selectByNameAndCategoryIds(StringUtils.isBlank(keyWord)?null:keyWord,
                categoryIdList.size()==0?null:categoryIdList);
        List<ProductListVo>productListVoList= Lists.newArrayList();
        for (Product product :productList)
        {
            ProductListVo productListVo=assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        PageInfo pageInfo =new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }

    private ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo=new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.isc.com/"));
        Category category =categoryDao.selectByPrimaryKey(product.getCategoryId());
        if(category==null){
            productDetailVo.setParentCategoryId(0);
        }else{
            productDetailVo.setParentCategoryId(category.getParentId());
        }
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }
}
