package com.bin.spark.model;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "user")
public class UserModel {

    @Id
    private Integer id;

    private Date createdAt;

    private Date updatedAt;

    private String telephone;

    private String password;

    private String nickName;

    private Integer gender;

}