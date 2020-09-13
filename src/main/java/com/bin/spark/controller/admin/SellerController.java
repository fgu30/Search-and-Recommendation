package com.bin.spark.controller.admin;

import com.bin.spark.common.AdminPermission;
import com.bin.spark.common.BusinessException;
import com.bin.spark.common.ResponseEnum;
import com.bin.spark.common.ResponseVo;
import com.bin.spark.form.PageQuery;
import com.bin.spark.form.SellerCreateForm;
import com.bin.spark.model.SellerModel;
import com.bin.spark.model.UserModel;
import com.bin.spark.service.SellerService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

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

    @RequestMapping("/createpage")
    @AdminPermission
    public ModelAndView createPage(){
        ModelAndView modelAndView = new ModelAndView("/admin/seller/create.html");
        modelAndView.addObject("CONTROLLER_NAME","seller");
        modelAndView.addObject("ACTION_NAME","create");
        return modelAndView;
    }


    @RequestMapping(value = "/create",method = RequestMethod.POST)
    @AdminPermission
    public String create(@Valid SellerCreateForm sellerCreateForm, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            throw new BusinessException(ResponseEnum.PARAM_ERROR.getCode(),
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        SellerModel sellerModel = new SellerModel();
        sellerModel.setName(sellerCreateForm.getName());
        sellerService.create(sellerModel);
        return "redirect:/admin/seller/index";
    }


    @RequestMapping(value="down",method = RequestMethod.POST)
    @AdminPermission
    @ResponseBody
    public ResponseVo<SellerModel> down(@RequestParam(value="id")Integer id) throws BusinessException {
        SellerModel sellerModel = sellerService.changeStatus(id,1);
        return ResponseVo.success(sellerModel);
    }

    @RequestMapping(value="up",method = RequestMethod.POST)
    @AdminPermission
    @ResponseBody
    public ResponseVo<SellerModel> up(@RequestParam(value="id")Integer id) throws BusinessException {
        SellerModel sellerModel = sellerService.changeStatus(id,0);
        return ResponseVo.success(sellerModel);
    }
}
