package com.itheima.service.impl;

import com.itheima.mapper.CategoryMapper;
import com.itheima.pojo.Category;
import com.itheima.service.CategoryService;
import com.itheima.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl  implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public void add(Category category) {
        //补充属性值
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());

        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        category.setCreateUser(userId);

        categoryMapper.add(category);
    }

    //分类列表
    @Override
    public List<Category> list() {
        //当前用户id
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        return categoryMapper.list(userId);
    }

    //分类详情
    @Override
    public Category findById(Integer id) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        Category c = categoryMapper.findById(id,userId);
        return c;
    }

    //更新分类
    @Override
    public void update(Category category) {
        // createUser 是 update 语句里 where 条件的一部分，必须由当前登录用户补上，
        // 不能信任请求体传的值，否则会改到别人的分类
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        category.setCreateUser(userId);
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.update(category);
    }

    //删除分类
    @Override
    public void delete(Integer id) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        categoryMapper.delete(id,userId);
    }
}
