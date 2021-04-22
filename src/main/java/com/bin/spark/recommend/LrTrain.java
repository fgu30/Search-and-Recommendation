package com.bin.spark.recommend;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.io.IOException;

/**
 * LR 逻辑回归
 * @author 斌~
 * @version 1.0
 * @date 2021/4/22 下午10:12
 */
public class LrTrain {
    public static void main(String[] args) throws IOException {
        //初始化spark运行环境
        SparkSession spark = SparkSession.builder().master("local").appName("SparkApp").getOrCreate();
        JavaRDD<String> csvFile = spark.read().textFile("file:///Users/mac/Desktop/data/feature.csv").toJavaRDD();

        JavaRDD<Row> ratingJavaRDD = csvFile.map(new Function<String, Row>() {
            @Override
            public Row call(String v1) throws Exception {
                v1 = v1.replace("\"","");
                String[] strArr = v1.split(",");
                return RowFactory.create(new Double(strArr[11]), Vectors.dense(Double.parseDouble(strArr[0]),
                        Double.parseDouble(strArr[1]),
                        Double.parseDouble(strArr[2]),
                        Double.parseDouble(strArr[3]),
                        Double.parseDouble(strArr[4]),
                        Double.parseDouble(strArr[5]),
                        Double.parseDouble(strArr[6]),
                        Double.parseDouble(strArr[7]),
                        Double.parseDouble(strArr[8]),
                        Double.parseDouble(strArr[9]),
                        Double.parseDouble(strArr[10])));
            }
        });
        StructType schema = new StructType(
            new StructField[]{
                    new StructField("label", DataTypes.DoubleType,false, Metadata.empty()),
                    new StructField("features",new VectorUDT(),false, Metadata.empty())
            }
        );

        Dataset<Row> data = spark.createDataFrame(ratingJavaRDD, schema);
        Dataset<Row>[] dataArr = data.randomSplit(new double[]{0.8,0.2});
        Dataset<Row> trainData = dataArr[0];
        Dataset<Row> testData = dataArr[1];

        LogisticRegression lr =
                new LogisticRegression().setMaxIter(10).setRegParam(0.3).setElasticNetParam(0.8).setFamily("multinomial");
        LogisticRegressionModel lrModel = lr.fit(trainData);
        //保存lrModel
        lrModel.save("file:///Users/mac/Desktop/data/lrModel");
        //测试评估
        Dataset<Row> predictions = lrModel.transform(testData);
        //评价指标
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator();
        double accuracy = evaluator.setMetricName("accuracy").evaluate(predictions);

        System.out.println("auc="+accuracy);
    }
}
