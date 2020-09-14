package com.bin.spark.service;

import com.bin.spark.model.CategoryModel;

import java.util.List;

/**
 * Created by 斌~
 * 2020/9/14 11:00
 */
public interface CategoryService {

    /**
     * 创建品类
     * @param categoryModel
     * @return
     */
    CategoryModel create (CategoryModel categoryModel);

    /**
     * 查询品类
     * @param id
     * @return
     */
    CategoryModel get(Integer id);

    /**
     * 品类列表
     * @return
     */
    List<CategoryModel> selectAll ();

    Integer countAllCategory();
}
