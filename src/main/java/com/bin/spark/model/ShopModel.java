package com.bin.spark.model;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;
@Data
@Table(name = "shop")
public class ShopModel {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    private Date createdAt;

    private Date updatedAt;

    private String name;

    private BigDecimal remarkScore;

    private Integer pricePerMan;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer categoryId;
    private CategoryModel categoryModel;

    private String tags;

    private String startTime;

    private String endTime;

    private String address;

    private Integer sellerId;
    private SellerModel sellerModel;

    private String iconUrl;

    private Integer distance;
}