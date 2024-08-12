package com.qokaerxn.controller.admin;

import com.qokaerxn.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
public class ShopController {

    private static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态状态")
    public Result setStatus(@PathVariable Integer status){
        redisTemplate.opsForValue().set(KEY,status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("获取店铺状态")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        return Result.success(status);
    }
}
