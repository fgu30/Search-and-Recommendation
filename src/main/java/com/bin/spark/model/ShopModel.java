package com.bin.spark.model;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
/**
 * @author mac
 */
@Data
@Table(name = "shop")
@ToString
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

    @Transient
    private Integer distance;
}