package com.qokaerxn.service.Imp;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qokaerxn.constant.MessageConstant;
import com.qokaerxn.constant.StatusConstant;
import com.qokaerxn.dto.DishDTO;
import com.qokaerxn.dto.SetmealDTO;
import com.qokaerxn.dto.SetmealPageQueryDTO;
import com.qokaerxn.entity.Dish;
import com.qokaerxn.entity.Setmeal;
import com.qokaerxn.entity.SetmealDish;
import com.qokaerxn.exception.DeletionNotAllowedException;
import com.qokaerxn.exception.SetmealEnableFailedException;
import com.qokaerxn.mapper.DishMapper;
import com.qokaerxn.mapper.SetmealDishMapper;
import com.qokaerxn.mapper.SetmealMapper;
import com.qokaerxn.result.PageResult;
import com.qokaerxn.service.SetmealService;
import com.qokaerxn.vo.DishItemVO;
import com.qokaerxn.vo.SetmealVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO){
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);

        Long id = setmeal.getId();

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(id);
        });

        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    public void delete(List<Long> ids) {
        ids.forEach(id ->{
            //看是否有启售中的套餐
            Setmeal setmeal = setmealMapper.getSetmealById(id);
            if(StatusConstant.ENABLE == setmeal.getStatus()){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });

        //如果没有启售的套餐那么就继续删除
        ids.forEach(id ->{
            setmealMapper.deleteById(id);
            //套餐关系表当中删除
            setmealDishMapper.deleteBySetmealId(id);
        });
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        //更新套餐
        setmealMapper.update(setmeal);

        //删除套餐关系表中的相关菜品
        Long id = setmeal.getId();
        setmealDishMapper.deleteBySetmealId(id);
        //更新套餐关系表（重新添加）
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //将新套餐获得的套餐Id赋值给新增套餐当中的菜品
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(id);
        });
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 套餐停售启售
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        if(status == StatusConstant.ENABLE){
            List<Dish> dishes = dishMapper.getBySetmealId(id);
            dishes.forEach(dish -> {
                if(dish.getStatus() == StatusConstant.ENABLE){
                    throw new SetmealEnableFailedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
                }
            });
        }

        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }
}
