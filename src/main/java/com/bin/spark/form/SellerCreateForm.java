package com.bin.spark.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author 斌~
 * @version 1.0
 * @date 2020/9/13 11:33 下午
 */
@Data
public class SellerCreateForm {

    @NotBlank(message = "商户名不能为空")
    private String name;

}
