package com.itheima.mapper;

import com.itheima.pojo.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    //新增
    @Insert("insert into category(category_name,category_alias,create_user,create_time,update_time)"+
            "values (#{categoryName},#{categoryAlias},#{createUser},#{createTime},#{updateTime})")
    void add(Category category);

    //查询
    @Select("select * from category where create_user=#{userId}")
    List<Category> list(Integer userId);

    //根据id查询（必须带上 create_user 条件，否则任何登录用户遍历 id 就能查看别人的分类）
    @Select("select * from category where id = #{id} and create_user = #{userId}")
    Category findById(@Param("id") Integer id, @Param("userId") Integer userId);

    //更新（同样要带 create_user，否则能改别人的分类；createUser 由 Service 从当前登录用户补上）
    @Update("update category set category_name=#{categoryName},category_alias=#{categoryAlias},update_time=#{updateTime} where id =#{id} and create_user=#{createUser}")
    void update(Category category);

    //删除（同样要带 create_user，否则能删别人的分类）
    @Delete("delete from category where id=#{id} and create_user=#{userId}")
    void delete(@Param("id") Integer id, @Param("userId") Integer userId);
}
