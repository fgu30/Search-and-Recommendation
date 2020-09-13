package com.bin.spark.controller.admin;

import com.bin.spark.common.AdminPermission;
import com.bin.spark.form.PageQuery;
import com.bin.spark.model.SellerModel;
import com.bin.spark.service.SellerService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author 斌~
 * @version 1.0
 * @date 2020/9/13 10:23 下午
 */
@Controller("/admin/seller")
@RequestMapping("/admin/seller")
public class SellerController {

    @Autowired
    private SellerService sellerService;

    /**
     * 商户列表
     * @param pageQuery
     * @return
     */
    @RequestMapping("/index")
    @AdminPermission
    public ModelAndView index(PageQuery pageQuery){
        PageHelper.startPage(pageQuery.getPage(),pageQuery.getSize());
        List<SellerModel> sellerModels = sellerService.selectAll();
        PageInfo<SellerModel> sellerModelPageInfo = new PageInfo<>(sellerModels);
        ModelAndView modelAndView =new ModelAndView("admin/seller/index");
        modelAndView.addObject("data",sellerModelPageInfo);
        modelAndView.addObject("CONTROLLER_NAME","seller");
        modelAndView.addObject("ACTION_NAME","index");
        return modelAndView;
    }
}
