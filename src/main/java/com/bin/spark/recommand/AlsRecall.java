package com.bin.spark.recommand;

import lombok.Data;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.Serializable;

/**
 * Created by 斌~
 * 2020/9/21 17:51
 */
public class AlsRecall {

    public static void main(String[] args) {
        //初始化spark运行环境
        SparkSession sparkSession = SparkSession.builder().master("local").appName("DianpingApp").getOrCreate();

        JavaRDD<String> csvFile = sparkSession.read().textFile("file:///Users/mac/Desktop/behavior.cvs").toJavaRDD();

        JavaRDD<Rating> ratingJavaRDD = csvFile.map(new Function<String, Rating>() {
            @Override
            public Rating call(String s) throws Exception {
                return Rating.parseRating(s);
            }
        });
        Dataset<Row> rating = sparkSession.createDataFrame(ratingJavaRDD, Rating.class);
        //将rating数据二八分
        Dataset<Row>[] datasets = rating.randomSplit(new double[]{0.8, 0.2});
    }

    @Data
    public static class Rating implements Serializable{
        private int  userId;
        private int shopId;
        private int rating;

        public Rating(int userId, int shopId, int rating) {
            this.userId = userId;
            this.shopId = shopId;
            this.rating = rating;
        }

        public static Rating parseRating(String str){
            str = str.replace("\"","");
            String[] strArr = str.split(",");
            int userId =Integer.parseInt(strArr[0]);
            int shopId =Integer.parseInt(strArr[1]);
            int rating =Integer.parseInt(strArr[2]);
            return new Rating(userId,shopId,rating);
        }
    }
}
