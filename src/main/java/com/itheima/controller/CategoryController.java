package com.itheima.controller;

import com.itheima.pojo.Category;
import com.itheima.pojo.Result;
import com.itheima.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    //新增分类
    @PostMapping
    public Result add(@RequestBody @Validated({Category.Add.class}) Category category) {
        categoryService.add(category);
        return Result.success();
    }

    //列表查询
    @GetMapping
    public Result<List<Category>> list() {
       List<Category> list = categoryService.list();
       return Result.success(list);
    }

    //获取分类详情
    @GetMapping("/detail")
    public Result<Category> detail(Integer id) {
        Category c = categoryService.findById(id);
        if (c == null) {
            //查不到时明确告诉前端。注意这里不区分"真不存在"和"是别人的分类"，
            //统一说"不存在"，否则会泄露"别人的分类是否存在"这一信息
            return Result.error("分类不存在");
        }
        return Result.success(c);
    }

    //更新分类
    @PutMapping
    public Result update(@RequestBody @Validated(Category.Update.class) Category category) {
        categoryService.update(category);
        return Result.success();
    }

    //删除分类
    @DeleteMapping
    public Result delete(@RequestParam Integer id) {
        categoryService.delete(id);
        return Result.success();
    }

}
