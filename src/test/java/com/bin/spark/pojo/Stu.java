package com.bin.spark.pojo;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @author 斌~
 * @version 1.0
 * @date 2021/5/2 下午11:27
 */
@Document(indexName = "stu", type = "_doc",shards = 3, replicas = 0)
@Data
@ToString
public class Stu {
    @Id
    private Long stuId;

    @Field(store = true)
    private String name;

    @Field(store = true)
    private Integer age;

    @Field(store = true,type = FieldType.Keyword)
    private String sign;

    @Field(store = true)
    private String desc;

    @Field(store = true)
    private Double stuMoney;
}
