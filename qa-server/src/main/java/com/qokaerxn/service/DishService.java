package com.qokaerxn.service;


import com.qokaerxn.dto.DishDTO;
import com.qokaerxn.dto.DishPageQueryDTO;
import com.qokaerxn.entity.Dish;
import com.qokaerxn.result.PageResult;
import com.qokaerxn.vo.DishVO;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface DishService {

    /**
     * 按照id批量删除菜品
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 和口味一起保存
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 更新菜品和口味
     * @param dishDTO
     */
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 获取菜品和口味
     * @param id
     * @return
     */
    DishVO getDishWithFlavor(Long id);

    /**
     * 获取菜品（含口味）
     * @param dish
     * @return
     */
    List<DishVO> getListWithFlavor(Dish dish);

    /**
     * 菜品停售，启售
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);
}
