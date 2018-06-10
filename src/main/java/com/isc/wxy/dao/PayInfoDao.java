package com.isc.wxy.dao;

import com.isc.wxy.domain.PayInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by XY W on 2018/5/23.
 */
@Mapper
public interface PayInfoDao {
    int deleteByPrimaryKey(Integer id);

    int insert(PayInfo record);

    int insertSelective(PayInfo record);

    PayInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PayInfo record);

    int updateByPrimaryKey(PayInfo record);
}
