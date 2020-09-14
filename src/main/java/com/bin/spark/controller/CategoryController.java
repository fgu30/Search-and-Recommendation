package com.bin.spark.controller;


import com.bin.spark.common.CommonRes;
import com.bin.spark.common.ResponseVo;
import com.bin.spark.model.CategoryModel;
import com.bin.spark.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller("/category")
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @ResponseBody
    @RequestMapping("/list")
    public ResponseVo<List<CategoryModel>> list(){
        List<CategoryModel> categoryModelList = categoryService.selectAll();
        return ResponseVo.success(categoryModelList);

    }

}
