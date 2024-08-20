package com.qokaerxn.service.Imp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qokaerxn.constant.MessageConstant;
import com.qokaerxn.constant.StatusConstant;
import com.qokaerxn.context.BaseContext;
import com.qokaerxn.dto.*;
import com.qokaerxn.entity.*;
import com.qokaerxn.exception.AddressBookBusinessException;
import com.qokaerxn.exception.OrderBusinessException;
import com.qokaerxn.exception.ShoppingCartBusinessException;
import com.qokaerxn.mapper.*;
import com.qokaerxn.result.PageResult;
import com.qokaerxn.service.OrderService;
import com.qokaerxn.utils.WeChatPayUtil;
import com.qokaerxn.vo.OrderPaymentVO;
import com.qokaerxn.vo.OrderStatisticsVO;
import com.qokaerxn.vo.OrderSubmitVO;
import com.qokaerxn.vo.OrderVO;
import com.qokaerxn.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 用户提交订单
     *
     * @param ordersSubmitDTO
     * @return
     */
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //1. 处理各种业务异常（地址簿为空、购物车数据为空）
        //判断用户地址是否为空
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //购物车是否为空
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if (list == null && list.size() > 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //2. 向订单表插入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setAddress(addressBook.getDetail());
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        ordersMapper.insert(orders);

        List<OrderDetail> orderDetails = new ArrayList<>();
        //3. 向订单明细表插入n条数据
        for (ShoppingCart cart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);
        //4. 清空当前用户的购物车数据
        shoppingCartMapper.deleteByUserId(userId);
        //5. 封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "QA-take-away外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");


        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        // 替代微信支付成功后的数据库订单状态更新，直接在这里更新了
        // 根据订单号查询当前用户的该订单
        Orders ordersDB = ordersMapper.getByNumberAndUserId(ordersPaymentDTO.getOrderNumber(), userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.TO_BE_CONFIRMED);
        orders.setPayStatus(Orders.PAID);
        orders.setCheckoutTime(LocalDateTime.now());
//        Orders orders = Orders.builder()
//                .id(ordersDB.getId())
//                .status(Orders.TO_BE_CONFIRMED) // 订单状态，待接单
//                .payStatus(Orders.PAID) // 支付状态，已支付
//                .checkoutTime(LocalDateTime.now()) // 更新支付时间
//                .build();

        ordersMapper.update(orders);

        return vo;
    }


    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单号查询当前用户的订单
        Orders ordersDB = ordersMapper.getByNumberAndUserId(outTradeNo, userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        ordersMapper.update(orders);

        //通过websocket向客户端浏览器推送消息 type orderId content
        Map map = new HashMap();
        map.put("type", 1); // 1表示来单提醒 2表示客户催单
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号：" + outTradeNo);

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 客户催单服务
     *
     * @param id
     */
    public void reminder(Long id) {
        //根据Id查询订单
        Orders ordersDB = ordersMapper.getById(id);
        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Map map = new HashMap();
        map.put("type", 2); //1表示来单提醒 2表示客户催单
        map.put("orderId", id);
        map.put("content", "订单号：" + ordersDB.getNumber());

        //通过websocket向客户端浏览器推送消息
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    /**
     * 历史订单查询
     *
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    public PageResult pageQuery4User(int pageNum, int pageSize, Integer status) {
        PageHelper.startPage(pageNum, pageSize);

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        Page<Orders> page = ordersMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();

        for (Orders orders : page) {
            //获取订单详情并返回
            Long orderId = orders.getId();
            List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDetailList(orderDetails);
            list.add(orderVO);
        }
        return new PageResult(page.getTotal(), list);
    }

    /**
     * 获取订单详情
     *
     * @param id
     * @return
     */
    public OrderVO details(Long id) {
        Orders orders = ordersMapper.getById(id);
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    /**
     * 用户取消订单
     *
     * @param id
     */
    public void userCancelById(Long id) {
        Orders orderDB = ordersMapper.getById(id);

        if (orderDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if (orderDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setUserId(orderDB.getUserId());

        //如果已经付过款，那么退款
        if (orderDB.getPayStatus().equals(Orders.CONFIRMED)) {
            //调用微信支付退款接口
            //    weChatPayUtil.refund(
            //            orderDB.getNumber(), //商户订单号
            //            orderDB.getNumber(), //商户退款单号
            //            new BigDecimal(0.01),//退款金额，单位 元
            //            new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        ordersMapper.update(orders);
    }

    /**
     * 用户再来一单
     *
     * @param id
     */
    public void repetition(Long id) {
        Long userId = BaseContext.getCurrentId();
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

        List<ShoppingCart> shoppingCarts = orderDetails.stream().map(x -> {

            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(x, shoppingCart, "id");

            return shoppingCart;

        }).collect(Collectors.toList());

        shoppingCartMapper.insertBatch(shoppingCarts);
    }

    /**
     * 按条件查询订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = ordersMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> orderVOList = getOrdersVOList(page);

        return new PageResult(page.getTotal(), orderVOList);

    }

    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();

        Integer toBeConfirmed = ordersMapper.getByStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = ordersMapper.getByStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = ordersMapper.getByStatus(Orders.DELIVERY_IN_PROGRESS);

        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);

        return orderStatisticsVO;
    }

    /**
     * 接单
     *
     * @param ordersConfirmDTO
     */
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(ordersConfirmDTO.getStatus())
                .build();

        ordersMapper.update(orders);
    }

    /**
     * 拒单
     *
     * @param ordersRejectionDTO
     */
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        Orders orderDB = ordersMapper.getById(ordersRejectionDTO.getId());

        //订单存在且状态为2（待接单或者付款）才能拒单
        if (orderDB == null || !orderDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Integer payStatus = orderDB.getPayStatus();
        if (payStatus == Orders.PAID) {
            //调用微信支付退款接口
            //weChatPayUtil.refund(
            //        orderDB.getNumber(), //商户订单号
            //        orderDB.getNumber(), //商户退款单号
            //        new BigDecimal(0.01),//退款金额，单位 元
            //        new BigDecimal(0.01));//原订单金额
            log.info("申请退款："+ orderDB);
            orderDB.setPayStatus(Orders.REFUND);
        }

        // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间
        Orders orders = new Orders();
        orders.setId(orderDB.getId());
        orders.setCancelTime(LocalDateTime.now());
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());

        ordersMapper.update(orders);

    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders ordersDB = ordersMapper.getById(ordersCancelDTO.getId());

        Integer payStatus = ordersDB.getPayStatus();
        //若订单已支付，那么退款
        if(payStatus == Orders.PAID){
            //调用微信支付退款接口
            //weChatPayUtil.refund(
            //        orderDB.getNumber(), //商户订单号
            //        orderDB.getNumber(), //商户退款单号
            //        new BigDecimal(0.01),//退款金额，单位 元
            //        new BigDecimal(0.01));//原订单金额
            log.info("申请退款："+ ordersDB);
            ordersDB.setPayStatus(Orders.REFUND);
        }

        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());

        ordersMapper.update(orders);

    }

    /**
     * 派送订单
     * @param id
     */
    public void delivery(Long id) {
        Orders ordersDB = ordersMapper.getById(id);

        //订单存在且状态为已下单才能派送订单
        if(ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //修改订单状态
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        ordersDB.setStatus(Orders.DELIVERY_IN_PROGRESS);

        ordersMapper.update(orders);

    }

    /**
     * 完成订单
     * @param id
     */
    public void complete(Long id) {

        Orders ordersDB = ordersMapper.getById(id);

        if(ordersDB ==null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();

        orders.setId(id);
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        ordersMapper.update(orders);

    }


    /**
     * 返回数据
     *
     * @param page
     * @return
     */
    private List<OrderVO> getOrdersVOList(Page<Orders> page) {
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();

        if(!CollectionUtils.isEmpty(ordersList)){
            for (Orders orders : ordersList) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrdersDetailAsString(orders);

                orderVO.setOrderDishes(orderDishes);

                orderVOList.add(orderVO);
            }
        }

        return orderVOList;
    }

    /**
     * 根据Id获得订单详情并将他们以字符串的形式返回
     *
     * @param orders
     * @return
     */
    private String getOrdersDetailAsString(Orders orders) {
        List<OrderDetail> orderDetailsList = orderDetailMapper.getByOrderId(orders.getId());

        List<String> orderDetailStringList = orderDetailsList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        return String.join("", orderDetailStringList);
    }

}
