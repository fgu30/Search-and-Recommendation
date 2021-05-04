package com.bin.spark.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * FIXME: 这里所有的字段必须和 elasticsearch 中的 foodie-items 的字段保持一致
 * PUT /foodie-items
 * {
 *   "settings": {
 *     "number_of_shards": 1,
 *     "number_of_replicas": 0
 *   },
 *   "mappings": {
 *     "properties": {
 *       "itemId":{"type":"text"},
 *       "itemName":{"type":"text","analyzer": "ik_max_word","search_analyzer": "ik_smart","fielddata": true},
 *       "imgUrl":{"type":"text"},
 * 	     "price":{"type":"integer"},
 *       "sellCounts":{"type":"integer"}
 *     }
 *   }
 * }
 * @author mac fielddata": true   当需要按照字符串进行排序  需要这个设置
 */
@Document(indexName = "foodie-items", type = "_doc", shards = 3, replicas = 0,createIndex = false)
@Data
public class FoodieItems {
    @Id
    @Field(store = true, type = FieldType.Text, index = false)
    private String itemId;

    @Field(store = true,type = FieldType.Text,fielddata = true)
    private String itemName;

    @Field(store = true,type = FieldType.Text,index = false)
    private String imgUrl;

    @Field(store = true, type = FieldType.Integer)
    private Integer price;

    @Field(store = true, type = FieldType.Integer)
    private Integer sellCounts;
}
