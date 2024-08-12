package com.qokaerxn.controller.admin;

import com.qokaerxn.dto.DishDTO;
import com.qokaerxn.dto.DishPageQueryDTO;
import com.qokaerxn.result.PageResult;
import com.qokaerxn.result.Result;
import com.qokaerxn.service.DishService;
import com.qokaerxn.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping()
    @ApiOperation("新增菜品")
    public Result save(DishDTO dishDTO){
        dishService.saveWithFlavor(dishDTO);
        //清理缓存数据
        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        PageResult pageResult = dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 菜品批量删除
     */
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids){
        dishService.deleteBatch(ids);
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据Id查询相关菜品（包含口味）
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据Id查询相关菜品（包含口味）")
    public Result<DishVO> getDish(@PathVariable Long id){
        DishVO dishVO = dishService.getDishWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     *修改菜品
     * @param dishDTO
     * @return
     */
   @PutMapping
   @ApiOperation("修改菜品")
    public Result updateWithFlavor(@RequestBody DishDTO dishDTO){
       dishService.updateWithFlavor(dishDTO);
       //清理缓存数据
       cleanCache("dish_*");
       return Result.success();
   }

    /**
     * 菜品起售停售
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        dishService.startOrStop(status, id);

        //将所有的菜品缓存数据清理掉，所有以dish_开头的key
        cleanCache("dish_*");

        return Result.success();
    }

   private void cleanCache(String pattern){
       Set keys = redisTemplate.keys(pattern);
       redisTemplate.delete(keys);
   }

}
