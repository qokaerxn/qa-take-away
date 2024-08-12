package com.qokaerxn.task;

import com.qokaerxn.entity.Orders;
import com.qokaerxn.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 处理超时订单
     */
    @Scheduled(cron = "0 * * * * ? ")//每15分钟查看并取消
    public void processOutOfTime(){
        log.info("正在处理超时订单：{}",LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        List<Orders> ordersList = ordersMapper.getByOrderTimeAndStatusLT(time, Orders.PENDING_PAYMENT);

        for (Orders orders : ordersList) {
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelTime(LocalDateTime.now());
            orders.setCancelReason("订单超时，自动取消");
            ordersMapper.update(orders);
        }
    }

    /**
     * 处理一直处于派送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨一点查看并取消
    public void processDeliveryOrder(){
        log.info("正在处理一直处于派送状态当中的订单:{}",LocalDateTime.now());


        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        List<Orders> ordersList = ordersMapper.getByOrderTimeAndStatusLT(time,Orders.DELIVERY_IN_PROGRESS);

        if(ordersList != null && ordersList.size() > 0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                ordersMapper.update(orders);
            }
        }
    }
}
