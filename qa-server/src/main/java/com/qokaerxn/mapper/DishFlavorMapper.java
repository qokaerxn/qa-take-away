package com.qokaerxn.mapper;

import com.qokaerxn.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品Id删除对应的口味
     * @param id
     */
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long id);

    /**
     *
     * @param id
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getFlavorsByDishId(Long id);

}
