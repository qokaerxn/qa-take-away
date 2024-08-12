package com.qokaerxn.mapper;

import com.github.pagehelper.Page;
import com.qokaerxn.annotation.AutoFill;
import com.qokaerxn.dto.SetmealPageQueryDTO;
import com.qokaerxn.entity.Dish;
import com.qokaerxn.entity.Setmeal;
import com.qokaerxn.enumeration.OperationType;
import com.qokaerxn.vo.DishItemVO;
import com.qokaerxn.vo.SetmealVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name,sd.copies,d.image,d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{id}")
    List<DishItemVO> getDishItemBySetmealId(Long id);

    /**
     * 新增套餐
     *
     * @param setmeal
     */
    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);

    /**
     * 根据Id删除套餐
     * @param id
     */
    @Delete("delete from setmeal where id = #{id}")
    void deleteById(Long id);

    /**
     * 根据Id查询套餐
     * @param id
     * @return
     */
    @Select("select * from setmeal where id = #{id}")
    Setmeal getSetmealById(Long id);

    /**
     * 更新套餐
     * @param setmeal
     */
    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据id查询套餐和套餐菜品关系
     * @param id
     * @return
     */
    SetmealVO getByIdWithDish(Long id);

    /**
     * 根据条件统计套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
