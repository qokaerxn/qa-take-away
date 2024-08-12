package com.qokaerxn.mapper;

import com.github.pagehelper.Page;
import com.qokaerxn.annotation.AutoFill;
import com.qokaerxn.dto.DishPageQueryDTO;
import com.qokaerxn.entity.Dish;
import com.qokaerxn.enumeration.OperationType;
import com.qokaerxn.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 新增菜品
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据Id查询菜品
     * @param id
     * @return
     */
    @Select("select * from dish where id = #{id}")
    Dish getDishById(Long id);

    /**
     * 根据Id删除菜品
     * @param id
     */
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    /**
     * 根据Id动态修改菜品数据
     * @param dish
     */
    @AutoFill(value=OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 动态查询菜品
     * @param dish
     * @return
     */
    List<Dish> list(Dish dish);

    /**
     * 根据套餐Id查询菜品
     * @param setmealId
     * @return
     */
    @Select("select * from dish left join setmeal_dish sd on dish.id = sd.dish_id where sd.id = #{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);

    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

}
