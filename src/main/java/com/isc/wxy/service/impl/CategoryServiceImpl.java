package com.isc.wxy.service.impl;

import com.isc.wxy.dao.CategoryDao;
import com.isc.wxy.domain.Category;
import com.isc.wxy.service.CategoryService;
import com.isc.wxy.vo.ServerResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by XY W on 2018/5/20.
 */
@Service
public class CategoryServiceImpl implements CategoryService {
    private Logger logger= LoggerFactory.getLogger(CategoryServiceImpl.class);
    @Autowired
    CategoryDao categoryDao;

    //添加类目
    @Override
    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if(parentId==null||StringUtils.isBlank(categoryName))
        {
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }
        Category category=new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
        int count=categoryDao.insert(category);
        if(count>0){
            return ServerResponse.createBySuccessMessage("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    //更新类目名字
    @Transactional
    public ServerResponse addCategoryName(Integer categoryId,String categoryName){
        if(categoryId==null||StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("更新品类参数错误");
        }
        Category category=new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int count=categoryDao.updateByPrimaryKeySelective(category);
        if(count>0){
            return ServerResponse.createBySuccessMessage("更新品类名字成功");
        }
        return ServerResponse.createByErrorMessage("更新品类名字失败");
    }

    //获取一个类目的平级子目录不递归
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){
            List<Category>categoryList=categoryDao.selectCategoryChildrenByParentId(categoryId);
            if(CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到");
            }
            return ServerResponse.createBySuccess(categoryList);
    }

    //递归获取一个类目及其子目录
    public ServerResponse<List<Integer>> getDeepChildrenCategory(Integer categoryId){
        Set<Category>categorySet=new HashSet<Category>() ;
        categorySet=findChildCategory(categorySet,categoryId);
        List<Integer>categoryIdList=new ArrayList<>();
        if(categoryId!=null){
            for (Category category :categorySet){
                categoryIdList.add(category.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }

    private Set<Category> findChildCategory(Set<Category>categorySet, Integer categoryId)
    {
        Category category =categoryDao.selectByPrimaryKey(categoryId);
        if(category!=null){
            categorySet.add(category);
        }//查找子节点，一定要有一个退出条件
        List<Category>categoryList=categoryDao.selectCategoryChildrenByParentId(categoryId);
        for (Category categoryItem :categoryList){
            findChildCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }
}
