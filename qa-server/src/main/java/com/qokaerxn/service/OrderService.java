package com.qokaerxn.service;

import com.qokaerxn.dto.OrdersPaymentDTO;
import com.qokaerxn.dto.OrdersSubmitDTO;
import com.qokaerxn.result.PageResult;
import com.qokaerxn.vo.OrderPaymentVO;
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

    void paySuccess(String outTradeNo);

    void reminder(Long id);

    PageResult pageQuery4User(int page, int pageSize, Integer status);

    OrderVO details(Long id);

    void userCancelById(Long id);

    void repetition(Long id);
}
