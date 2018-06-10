package com.isc.wxy.service;

import com.isc.wxy.domain.Category;
import com.isc.wxy.vo.ServerResponse;

import java.util.List;

/**
 * Created by XY W on 2018/5/20.
 */
public interface CategoryService {
     ServerResponse addCategory(String categoryName, Integer parentId);
     ServerResponse addCategoryName(Integer categoryId,String categoryName);
     ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId);
     ServerResponse<List<Integer>> getDeepChildrenCategory(Integer categoryId);
}
