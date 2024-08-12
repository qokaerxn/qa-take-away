package com.qokaerxn.service.Imp;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qokaerxn.constant.MessageConstant;
import com.qokaerxn.constant.StatusConstant;
import com.qokaerxn.dto.DishDTO;
import com.qokaerxn.dto.DishPageQueryDTO;
import com.qokaerxn.entity.Dish;
import com.qokaerxn.entity.DishFlavor;
import com.qokaerxn.entity.Setmeal;
import com.qokaerxn.entity.SetmealDish;
import com.qokaerxn.exception.DeletionNotAllowedException;
import com.qokaerxn.mapper.DishFlavorMapper;
import com.qokaerxn.mapper.DishMapper;
import com.qokaerxn.mapper.SetmealDishMapper;
import com.qokaerxn.mapper.SetmealMapper;
import com.qokaerxn.result.PageResult;
import com.qokaerxn.service.DishService;
import com.qokaerxn.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setMealDishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 根据菜品Id批量删除菜品
     *
     * @param ids
     */
    @Override
    public void deleteBatch(List<Long> ids) {
        //判断菜品是否正在被售卖当中
        for (Long id : ids) {
            Dish dish = dishMapper.getDishById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断菜品是否被关联到套餐当中
        List<Long> setMealDishIds = setMealDishMapper.getSetMealIdsByDishIds(ids);
        if (setMealDishIds != null && setMealDishIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        for (Long id : ids) {
            dishMapper.deleteById(id);
        }
    }

    /**
     * 新增菜品（含口味）
     *
     * @param dishDTO
     */
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        //获取dish的Id值
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();

        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {

        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        long total = page.getTotal();

        List<DishVO> result = page.getResult();

        return new PageResult(total, result);

    }

    /**
     * 修改菜品信息（包括口味）
     *
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        //删除原有的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //从DTO中获取要插入的口味的值
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 根据菜品Id获取菜品
     *
     * @param id
     * @return
     */
    @Override
    public DishVO getDishWithFlavor(Long id) {
        DishVO dishVO = new DishVO();
        Dish dish = dishMapper.getDishById(id);
        BeanUtils.copyProperties(dish, dishVO);
        List<DishFlavor> flavors = dishFlavorMapper.getFlavorsByDishId(id);

        dishVO.setFlavors(flavors);
        return dishVO;

    }


    /**
     * 用户获取某一种类菜品
     *
     * @param dish
     * @return
     */
    @Override
    public List<DishVO> getListWithFlavor(Dish dish) {
        //先获取符合条件的菜品
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        //遍历菜品集
        for (Dish dish1 : dishList) {
            //找到每个菜品对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getFlavorsByDishId(dish1.getId());
            //给要返回的数据赋值
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish1, dishVO);
            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }
        return dishVOList;
    }


    /**
     * 菜品停售，启售
     *
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);

        if (StatusConstant.DISABLE == status) {
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            List<Long> setmealIds = setMealDishMapper.getSetMealIdsByDishIds(dishIds);
            if(setmealIds != null && setmealIds.size() > 0){
                for (Long setmealId : setmealIds) {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);
                }
            }
        }
    }

}
