package com.isc.wxy.dao;

import com.isc.wxy.domain.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by XY W on 2018/5/24.
 */
@Mapper
public interface OrderItemDao {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    List<OrderItem> getByOrderNoUserId(@Param("orderNo") Long orderNo, @Param("userId") Integer userId);

    List<OrderItem> getByOrderNo(@Param("orderNo") Long orderNo);

    OrderItem getByProductIdUserId(@Param("productId") Integer productId, @Param("userId") Integer userId);


    void batchInsert(@Param("orderItemList") List<OrderItem> orderItemList);
}
