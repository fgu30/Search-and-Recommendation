package com.bin.spark.model;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author mac
 */
@Table(name = "seller")
@Data
public class SellerModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private Date createdAt;

    private Date updatedAt;

    private BigDecimal remarkScore;

    private Integer disabledFlag;


}