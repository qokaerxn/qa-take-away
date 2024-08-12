package com.qokaerxn.controller.user;

import com.qokaerxn.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "用户店铺管理接口")
@Slf4j
public class ShopController {
    private static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/status")
    @ApiOperation("用户获取店铺状态")
    public Result<Integer> getStatus(){
        log.info("用户获取店铺状态");
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        return Result.success(status);
    }
}
