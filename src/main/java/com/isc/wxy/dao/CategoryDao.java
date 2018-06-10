package com.isc.wxy.dao;

import com.isc.wxy.domain.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created by XY W on 2018/5/20.
 */
@Mapper
public interface CategoryDao {
    int deleteByPrimaryKey(Integer id);

    int insert(Category record);

    int insertSelective(Category record);

    Category selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Category record);

    int updateByPrimaryKey(Category record);

    List<Category> selectCategoryChildrenByParentId(Integer parentId);
}
