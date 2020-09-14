package com.bin.spark.service.impl;

import com.bin.spark.mapper.CategoryModelMapper;
import com.bin.spark.model.CategoryModel;
import com.bin.spark.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by 斌~
 * 2020/9/14 11:02
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryModelMapper categoryModelMapper;

    /**
     * 创建品类
     *
     * @param categoryModel
     * @return
     */
    @Override
    @Transactional
    public CategoryModel create(CategoryModel categoryModel) {
        //校验品类名称不能重复
        Example example = new Example(CategoryModel.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("name", categoryModel.getName());
        List<CategoryModel> categoryModelList = categoryModelMapper.selectByExample(example);
        if(!CollectionUtils.isEmpty(categoryModelList)){
            throw new RuntimeException("品类名已经存在");
        }
        //创建品类
        categoryModel.setCreatedAt(new Date());
        categoryModel.setUpdatedAt(new Date());
        categoryModelMapper.insertSelective(categoryModel);

        return categoryModel;
    }

    /**
     * 查询品类
     *
     * @param id
     * @return
     */
    @Override
    public CategoryModel get(Integer id) {
        CategoryModel categoryModel = categoryModelMapper.selectByPrimaryKey(id);
        return categoryModel;
    }

    /**
     * 品类列表
     *
     * @return
     */
    @Override
    public List<CategoryModel> selectAll() {
        List<CategoryModel> categoryModelList = categoryModelMapper.selectAll();
        return categoryModelList.stream()
                .sorted(Comparator.comparing(CategoryModel::getSort).reversed())
                .sorted(Comparator.comparing(CategoryModel::getId)).collect(Collectors.toList());
    }

    @Override
    public Integer countAllCategory() {
        CategoryModel categoryModel = new CategoryModel();
        return categoryModelMapper.selectCount(categoryModel);
    }
}
