package com.isc.wxy.dao;

import com.isc.wxy.domain.Shipping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by XY W on 2018/5/23.
 */
@Mapper
public interface ShippingDao {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    int deleteByShippingIdUserId(@Param("userId") Integer userId, @Param("shippingId") Integer shippingId);

    int updateByShipping(Shipping record);

    Shipping selectByShippingIdUserId(@Param("userId") Integer userId, @Param("shippingId") Integer shippingId);

    List<Shipping> selectByUserId(@Param("userId") Integer userId);
}
