package com.bin.spark.recommend;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.api.java.function.Function;


import java.io.IOException;
import java.io.Serializable;

/**
 * @author 斌~
 * @version 1.0
 * @date 2021/4/10 下午4:57
 */
public class AlsRecall implements Serializable {
    public static void main(String[] args) throws IOException {
        //初始化spark运行环境
        SparkSession spark = SparkSession.builder()
                .master("local")
                .appName("DianpingApp")
                .getOrCreate();
        JavaRDD<String> csvFile = spark.read().textFile("file:///Users/mac/Desktop/data/behavior.csv").toJavaRDD();
        JavaRDD<Rating> ratingJavaRDD = csvFile.map(new Function<String, Rating>() {
            @Override
            public Rating call(String s) throws Exception {
                return Rating.parseRating(s);
            }
        });
        Dataset<Row> ratings = spark.createDataFrame(ratingJavaRDD, Rating.class);
        //将所有的rating数据28分,也就是80%数据做训练，20%做测试
        Dataset<Row>[] splits = ratings.randomSplit(new double[]{0.8, 0.2});

        Dataset<Row> trainingData = splits[0];
        Dataset<Row> testData = splits[1];

        ALS als = new ALS()
                .setMaxIter(10)     //最大迭代次数
                .setRank(5)         //分解出5个特征
                //正则化系数，防止过拟合，也就是训练出来的数据过分趋近于真实数据，一旦真实数据有误差，模型预测结果反而不尽如人意
                //如何防止？增大数据规模，减少特征的维度，增大正则化系数
                //欠拟合：增加维度，减少正则化数
                .setRegParam(0.01)
                .setUserCol("userId")
                .setItemCol("shopId")
                .setRatingCol("rating");

        //模型训练
        ALSModel alsModel = als.fit(trainingData);
        alsModel.save("file:///Users/mac/Desktop/data/als");
    }

    public static class Rating implements Serializable{
        private int userId;
        private int shopId;
        private int rating;

        private static Rating parseRating(String str){
            str = str.replace("\"" , "");
            String[] strArr = str.split(",");
            int userId = Integer.parseInt(strArr[0]);
            int shopId = Integer.parseInt(strArr[1]);
            int rating = Integer.parseInt(strArr[2]);
            return new Rating(userId , shopId , rating);
        }
        public Rating(int userId, int shopId, int rating) {
            this.userId = userId;
            this.shopId = shopId;
            this.rating = rating;
        }
        public int getUserId() {
            return userId;
        }
        public int getShopId() {
            return shopId;
        }
        public int getRating() {
            return rating;
        }
    }
}
