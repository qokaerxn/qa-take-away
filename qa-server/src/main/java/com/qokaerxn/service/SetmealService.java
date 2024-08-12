package com.qokaerxn.service;

import com.qokaerxn.dto.DishDTO;
import com.qokaerxn.dto.SetmealDTO;
import com.qokaerxn.dto.SetmealPageQueryDTO;
import com.qokaerxn.entity.Setmeal;
import com.qokaerxn.result.PageResult;
import com.qokaerxn.vo.DishItemVO;
import com.qokaerxn.vo.SetmealVO;
import java.util.List;

public interface SetmealService {

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);

    void saveWithDish(SetmealDTO setmealDTO);

    void delete(List<Long> ids);

    void update(SetmealDTO setmealDTO);

    void startOrStop(Integer status, Long id);
}
