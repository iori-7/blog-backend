package com.itheima.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.mapper.ArticleMapper;
import com.itheima.pojo.Article;
import com.itheima.pojo.PageBean;
import com.itheima.service.ArticleService;
import com.itheima.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public void add(Article article) {
        //补充属性值
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());

        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userid = (Integer)map.get("id");
        article.setCreateUser(userid);

        articleMapper.add(article);
    }

    @Override
    public PageBean<Article> list(Integer pageNum, Integer pageSize, Integer categoryId, String state) {
        //分页参数兜底与上限保护：
        //pageSize 不设上限的话，前端传 pageSize=1000000 就等于一次全表查询
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        //创建PageBean对象
        PageBean<Article> pageBean = new PageBean<>();
        //开启分页查询
        PageHelper.startPage(pageNum, pageSize);
        //调用Mapper
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        List<Article> as = articleMapper.list(userId,categoryId,state);
        //Page中提供了方法，可以获取PageHelper分页查询后得到的总记录条数和和当前页数据
        Page<Article> p=(Page<Article>) as;
        //把数据填充到PageBean
        pageBean.setTotal(p.getTotal());
        pageBean.setItems(p.getResult());
        return pageBean;
    }

    //获取文章详情
    @Override
    public Article detail(Integer id) {
        return articleMapper.detail(id);
    }

    //更新文章
    @Override
    public void update(Article article) {
        //补充属性值
        //分类id 必须用请求体里前端选中的那个，不能拿 ThreadLocal 里的用户id 去覆盖
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.update(article);
    }

    //删除文章
    @Override
    public void delete(Integer id) {
        articleMapper.delete(id);
    }

}
