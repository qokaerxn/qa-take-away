package com.qokaerxn.service;

import com.qokaerxn.dto.*;
import com.qokaerxn.result.PageResult;
import com.qokaerxn.vo.OrderPaymentVO;
import com.qokaerxn.vo.OrderStatisticsVO;
import com.qokaerxn.vo.OrderSubmitVO;
import com.qokaerxn.vo.OrderVO;

public interface OrderService {

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 订单提醒
     * @param id
     */
    void reminder(Long id);

    /**
     * 用户订单分页查询
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    PageResult pageQuery4User(int page, int pageSize, Integer status);

    /**
     * 订单详情
     * @param id
     * @return
     */
    OrderVO details(Long id);

    /**
     * 用户取消订单
     * @param id
     */
    void userCancelById(Long id);

    /**
     * 再来一单
     * @param id
     */
    void repetition(Long id);

    /**
     * 订单根据条件分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     *各个状态订单查询
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     *接单
     * @param ordersConfirmDTO
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    void cancel(OrdersCancelDTO ordersCancelDTO);

    /**
     * 派送订单
     * @param id
     */
    void delivery(Long id);

    /**
     * 完成订单
     * @param id
     */
    void complete(Long id);
}
