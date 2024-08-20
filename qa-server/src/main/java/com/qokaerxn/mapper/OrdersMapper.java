package com.qokaerxn.mapper;

import com.github.pagehelper.Page;
import com.qokaerxn.dto.GoodsSalesDTO;
import com.qokaerxn.dto.OrdersPageQueryDTO;
import com.qokaerxn.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrdersMapper {

    /**
     * 分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     *
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号和用户id查询订单
     * @param orderNumber
     * @param userId
     */
    @Select("select * from orders where number = #{orderNumber} and user_id= #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    @Select("select * from orders where status = #{pendingPayment} and order_time < #{time}")
    List<Orders> getByOrderTimeAndStatusLT(LocalDateTime time, Integer pendingPayment);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    Double getSumOfDayByMap(Map map);

    Integer countByMap(Map map);

    List<GoodsSalesDTO> getTop10Orders(LocalDateTime beginTime,LocalDateTime endTime);

    /**
     * 根据状态查询订单数
     * @param status
     * @return
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer getByStatus(Integer status);
}
