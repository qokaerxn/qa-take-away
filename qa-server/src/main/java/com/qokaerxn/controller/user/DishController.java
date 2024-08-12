package com.qokaerxn.controller.user;

import com.qokaerxn.constant.StatusConstant;
import com.qokaerxn.entity.Dish;
import com.qokaerxn.result.Result;
import com.qokaerxn.service.DishService;
import com.qokaerxn.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Api(tags = "用户菜品控制接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/list")
    @ApiOperation("用户获取某类菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        String key = "dish_" + categoryId;
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);

        if (list != null && list.size() > 0) {
            return Result.success(list);
        }

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);
        list = dishService.getListWithFlavor(dish);

        redisTemplate.opsForValue().set(key,list);

        return Result.success(list);
    }

}
