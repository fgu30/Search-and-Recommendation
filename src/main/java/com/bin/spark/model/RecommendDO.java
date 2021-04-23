package com.bin.spark.model;

import lombok.Data;

import javax.persistence.Table;

@Data
@Table(name = "recommend")
public class RecommendDO {
    private Integer id;

    private String recommend;

}