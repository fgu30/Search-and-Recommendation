package com.bin.spark.recommend;

import lombok.Data;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.io.Serializable;

/**
 * ALS召回算法的训练
 * Created by 斌~
 * 2020/9/21 17:51
 * @author mac
 */
public class AlsRecallTrain implements Serializable {

    public static void main(String[] args) throws IOException {
        //        String path = "file:///C:\\java\\gitcode\\staging\\src\\main\\resources\\alsmodel";
        //初始化spark运行环境
        SparkSession spark = SparkSession.builder().master("local").appName("SparkApp").getOrCreate();
        JavaRDD<String> csvFile = spark.read().textFile("file:///Users/mac/Desktop/data/behavior.csv").toJavaRDD();
        JavaRDD<Rating> ratingJavaRDD = csvFile.map(new Function<String, Rating>() {
            @Override
            public Rating call(String str) throws Exception {
                return Rating.parseRating(str);
            }
        });
        //变为DataSet<Row> 数据就写入spark计算集合系统中了
        Dataset<Row> rating = spark.createDataFrame(ratingJavaRDD,Rating.class);
        //将所有的rating数据分成82份
        Dataset<Row>[] ratings = rating.randomSplit(new double[]{0.8,0.2});
        Dataset<Row> trainingData = ratings[0];
        Dataset<Row> testingData = ratings[1];
        //过拟合：增大数据规模、减少rank，增大正则化的系数
        //欠拟合：增加rank，减少正则化系数
        ALS als = new ALS().setMaxIter(10).setRank(5).setRegParam(0.01).
                setUserCol("userId").setItemCol("shopId").setRatingCol("rating");
        //模型训练
        ALSModel alsModel = als.fit(trainingData);
        //模型评测
        Dataset<Row> predictions = alsModel.transform(testingData);
        //rmse 均方差根误差，预测值与真实值之间误差的平方和除以观测次数，开个根号
        RegressionEvaluator evaluator = new RegressionEvaluator().setMetricName("rmse")
                .setLabelCol("rating").setPredictionCol("prediction");
        double rmse = evaluator.evaluate(predictions);
        System.out.println("rmse = "+rmse);
        //保存数据模型
        alsModel.save("file:///Users/mac/Desktop/data/alsModel");
    }

    public static class Rating implements Serializable{
        private int userId;
        private int shopId;
        private int rating;
        //处理csv数据
        public static Rating parseRating(String str){
            Rating rating = new Rating();
            str = str.replaceAll("\"", "");
            String[] strArr = str.split(",");
            rating.setUserId(Integer.parseInt(strArr[0]));
            rating.setShopId(Integer.parseInt(strArr[1]));
            rating.setRating(Integer.parseInt(strArr[2]));
            return rating;
        }
        /**
         * @return the userId
         */
        public int getUserId() {
            return userId;
        }
        /**
         * @param userId the userId to set
         */
        public void setUserId(int userId) {
            this.userId = userId;
        }
        /**
         * @return the shopId
         */
        public int getShopId() {
            return shopId;
        }
        /**
         * @param shopId the shopId to set
         */
        public void setShopId(int shopId) {
            this.shopId = shopId;
        }
        /**
         * @return the rating
         */
        public int getRating() {
            return rating;
        }
        /**
         * @param rating the rating to set
         */
        public void setRating(int rating) {
            this.rating = rating;
        }
    }
}