package com.qokaerxn.service.Imp;

import com.qokaerxn.dto.ShoppingCartDTO;
import com.qokaerxn.entity.Dish;
import com.qokaerxn.entity.Setmeal;
import com.qokaerxn.entity.ShoppingCart;
import com.qokaerxn.context.BaseContext;
import com.qokaerxn.mapper.DishMapper;
import com.qokaerxn.mapper.SetmealMapper;
import com.qokaerxn.mapper.ShoppingCartMapper;
import com.qokaerxn.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 往购物车当中添加商品
     * @param shoppingCartDTO
     */
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断新添加的商品是否在购物车当中已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        log.info("看看有没有显示我的用户Id:"+userId);
        shoppingCart.setUserId(userId);

        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);

        if (shoppingCarts != null && shoppingCarts.size() > 0) {
            //如果存在，那么将购物车关系表当中的数量加1
            ShoppingCart shoppingCart1 = shoppingCarts.get(0);
            shoppingCart1.setNumber(shoppingCart1.getNumber() + 1);
            shoppingCartMapper.updateCartNumber(shoppingCart1);
        } else {
            //判断加入购物车的是单个商品还是套餐
            Long dishId = shoppingCart.getDishId();
            if (dishId != null) {
                //插入的是单个商品
                Dish dish = dishMapper.getDishById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else {
                //插入的是套餐
                Setmeal setmeal = setmealMapper.getSetmealById(shoppingCart.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
        }

        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCart.setNumber(1);
        shoppingCartMapper.insert(shoppingCart);
    }

    /**
     * 查看购物车
     * @return
     */
    public List<ShoppingCart> showShoppingCart() {
        Long userId = BaseContext.getCurrentId();

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    /**
     * 清空购物车
     */
    public void cleanShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }

    /**
     * 从购物车当中删除一个商品
     * @param shoppingCartDTO
     */
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        //设置查询条件，查询当前登录用户的购物车数据
        shoppingCart.setUserId(BaseContext.getCurrentId());

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if(list != null && list.size() > 0){
            shoppingCart = list.get(0);

            Integer number = shoppingCart.getNumber();
            if(number == 1){
                //当前商品在购物车中的份数为1，直接删除当前记录
                shoppingCartMapper.deleteById(shoppingCart.getId());
            }else {
                //当前商品在购物车中的份数不为1，修改份数即可
                shoppingCart.setNumber(shoppingCart.getNumber() - 1);
                shoppingCartMapper.updateCartNumber(shoppingCart);
            }
        }
    }
}
